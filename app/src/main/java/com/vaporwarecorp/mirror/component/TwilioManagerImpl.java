/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.twilio.common.AccessManager;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.LogLevel;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.sdk.auth.AccessToken;
import com.twilio.sdk.auth.ConversationsGrant;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.event.ResetEvent;
import com.vaporwarecorp.mirror.event.TwilioEvent;
import com.vaporwarecorp.mirror.feature.twilio.TwilioPresenter;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static com.vaporwarecorp.mirror.util.RxUtil.delay;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(TwilioManager.class)
public class TwilioManagerImpl extends AbstractManager implements TwilioManager {
// ------------------------------ FIELDS ------------------------------

    private static final int MAX_LOGIN_RETRIES = 3;
    private static final String PREF = TwilioManager.class.getName();
    private static final String PREF_ACCOUNT_SID = PREF + ".PREF_ACCOUNT_SID";
    private static final String PREF_API_KEY_SECRET = PREF + ".PREF_API_KEY_SECRET";
    private static final String PREF_API_KEY_SID = PREF + ".PREF_API_KEY_SID";
    private static final String PREF_IDENTITY = PREF + ".PREF_IDENTITY";
    private static final String PREF_PROFILE_SID = PREF + ".PREF_PROFILE_SID";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private AccessManager mAccessManager;
    private String mAccountSID;
    private String mApiKeySID;
    private String mApiKeySecret;
    private TwilioConversationsClient mConversationsClient;
    private String mIdentity;
    private IncomingInvite mIncomingInvite;
    private int mLoginRetries;
    private String mProfileSID;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/twilio.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("profileSID", mProfileSID)
                .put("accountSID", mAccountSID)
                .put("apiKeySID", mApiKeySID)
                .put("apiKeySecret", mApiKeySecret)
                .put("identity", mIdentity)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        stop();
        mConfigurationManager.updateString(PREF_PROFILE_SID, jsonNode, "profileSID");
        mConfigurationManager.updateString(PREF_ACCOUNT_SID, jsonNode, "accountSID");
        mConfigurationManager.updateString(PREF_API_KEY_SID, jsonNode, "apiKeySID");
        mConfigurationManager.updateString(PREF_API_KEY_SECRET, jsonNode, "apiKeySecret");
        mConfigurationManager.updateString(PREF_IDENTITY, jsonNode, "identity");
        loadConfiguration();
        start();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// --------------------- Interface TwilioManager ---------------------

    @Override
    public IncomingInvite getCurrentIncomingInvite() {
        return mIncomingInvite;
    }

    public void start() {
        Timber.d("start");
        if (isEmpty(mProfileSID) || TwilioConversationsClient.isInitialized()) {
            return;
        }

        Timber.d("initialize TwilioConversationsClient");
        TwilioConversationsClient.setLogLevel(LogLevel.ERROR);
        TwilioConversationsClient.initialize(mAppManager.getAppContext());

        authorizeClient();
    }

    public void stop() {
        Timber.d("stop");
        mIncomingInvite = null;

        TwilioConversationsClient.destroy();

        if (mAccessManager != null) {
            mAccessManager.dispose();
            mAccessManager = null;
        }
    }

    /**
     * AccessManager listener
     */
    private AccessManager.Listener accessManagerListener() {
        Timber.d("accessManagerListener()");
        return new AccessManager.Listener() {
            @Override
            public void onTokenExpired(AccessManager twilioAccessManager) {
                Timber.i("onTokenExpired");
                authorizeClient();
            }

            @Override
            public void onTokenUpdated(AccessManager twilioAccessManager) {
                Timber.i("onTokenUpdated");
                startConversationsClient();
            }

            @Override
            public void onError(AccessManager twilioAccessManager, String s) {
                Timber.e("onError %s", s);
            }
        };
    }

    private void authorizeClient() {
        Timber.d("authorizeClient");
        // create a grant
        final ConversationsGrant grant = new ConversationsGrant();
        grant.setConfigurationProfileSid(mProfileSID);

        // Create an Access Token
        final AccessToken token = new AccessToken.Builder(mAccountSID, mApiKeySID, mApiKeySecret)
                .identity(mIdentity) // Set the Identity of this token
                .grant(grant) // Grant access to Conversations
                .build();

        // now authorize the client
        mAccessManager = AccessManager.create(mAppManager.getAppContext(), token.toJWT(), accessManagerListener());
    }

    /**
     * ConversationsClient listener
     */
    private TwilioConversationsClient.Listener conversationsClientListener() {
        Timber.d("conversationsClientListener()");
        return new TwilioConversationsClient.Listener() {
            @Override
            public void onStartListeningForInvites(TwilioConversationsClient conversationsClient) {
                Timber.i("onStartListeningForInvites");
                mLoginRetries = 0;
                mIncomingInvite = null;
            }

            @Override
            public void onStopListeningForInvites(TwilioConversationsClient conversationsClient) {
                Timber.i("onStopListeningForInvites");
            }

            @Override
            public void onFailedToStartListening(TwilioConversationsClient conversationsClient,
                                                 TwilioConversationsException e) {
                Timber.e(e, "onFailedToStartListening: %s login retries", mLoginRetries);
                if (mLoginRetries < MAX_LOGIN_RETRIES) {
                    mLoginRetries++;
                    delay(l -> authorizeClient(), 20);
                } else {
                    stop();
                }
            }

            @Override
            public void onIncomingInvite(TwilioConversationsClient conversationsClient,
                                         IncomingInvite incomingInvite) {
                Timber.i("onIncomingInvite");
                mIncomingInvite = incomingInvite;
                mEventManager.post(new TwilioEvent(incomingInvite));
            }

            @Override
            public void onIncomingInviteCancelled(TwilioConversationsClient conversationsClient,
                                                  IncomingInvite incomingInvite) {
                Timber.i("onIncomingInviteCancelled");
                mIncomingInvite = null;
                mEventManager.post(new ResetEvent(TwilioPresenter.class));
            }
        };
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mLoginRetries = 0;
        mProfileSID = mConfigurationManager.getString(PREF_PROFILE_SID, "");
        mAccountSID = mConfigurationManager.getString(PREF_ACCOUNT_SID, "");
        mApiKeySID = mConfigurationManager.getString(PREF_API_KEY_SID, "");
        mApiKeySecret = mConfigurationManager.getString(PREF_API_KEY_SECRET, "");
        mIdentity = mConfigurationManager.getString(PREF_IDENTITY, "");
    }

    /**
     * Start the TwilioConversationsClient.
     */
    private void startConversationsClient() {
        if (mConversationsClient != null) {
            return;
        }

        mConversationsClient = TwilioConversationsClient.create(mAccessManager, conversationsClientListener());
        mConversationsClient.listen();
    }
}
