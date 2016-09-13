/*
 *   Copyright 2016 Johann Reyes
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vaporwarecorp.mirror.feature.artik;

import android.content.Intent;
import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.event.ApplicationEvent;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.Map;

import static com.vaporwarecorp.mirror.event.ApplicationEvent.TRY_TO_START;
import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ArtikManager.class)
public class ArtikManagerImpl extends AbstractMirrorManager implements ArtikManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = ArtikManager.class.getName();
    private static final String PREF_APPLICATION_ID = PREF + ".PREF_APPLICATION_ID";
    private static final String PREF_CONTROL_DEVICE_ID = PREF + ".PREF_CONTROL_DEVICE_ID";
    private static final String PREF_SIDEKICK_DEVICE_ID = PREF + ".PREF_SIDEKICK_DEVICE_ID";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;

    private String mAccessToken;
    private String mApplicationId;
    private ArtikCloudManager mArtikCloudManager;
    private ArtikOAuthManager mArtikOAuthManager;
    private String mControlDeviceId;
    private boolean mEnabled;
    private String mSidekickDeviceId;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ArtikManager ---------------------

    @Override
    public void share(Map<String, Object> content) {
        if (mArtikCloudManager == null) {
            return;
        }
        subscribe(mArtikCloudManager.sendAction(content)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error trying to send action");
                    }

                    @Override
                    public void onNext(String s) {
                    }
                }));
    }

// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/artik.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("applicationId", mApplicationId)
                .put("controlDeviceId", mControlDeviceId)
                .put("sidekickDeviceId", mSidekickDeviceId)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_APPLICATION_ID, jsonNode, "applicationId");
        mConfigurationManager.updateString(PREF_CONTROL_DEVICE_ID, jsonNode, "controlDeviceId");
        mConfigurationManager.updateString(PREF_SIDEKICK_DEVICE_ID, jsonNode, "sidekickDeviceId");
        loadConfiguration();
        onFeatureResume();
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        if (mArtikCloudManager != null) {
            mArtikCloudManager.stopMessaging();
            mArtikCloudManager = null;
        }
    }

    @Override
    public void onFeatureResume() {
        if (mEnabled) {
            mArtikCloudManager = new ArtikCloudManager(mEventManager, mApplicationId, mSidekickDeviceId,
                    mControlDeviceId, mAccessToken);
            mArtikCloudManager.startMessaging();
        }
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    @Override
    public void onUnplugged(PluginBus bus) {
        mArtikCloudManager = null;
        mArtikOAuthManager = null;
        super.onUnplugged(bus);
    }

// --------------------- Interface WebAuthentication ---------------------

    @Override
    public void doAuthentication() {
        if(!mEnabled) {
            mEventManager.post(new ApplicationEvent(TRY_TO_START));
            return;
        }

        // get authorization
        subscribe(mArtikOAuthManager.authorizeImplicitly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted");
                        mEventManager.post(new ApplicationEvent(TRY_TO_START));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error trying to authenticate");
                    }

                    @Override
                    public void onNext(String s) {
                        mAccessToken = s;
                    }
                }));
    }

    @Override
    public void isAuthenticated(IsAuthenticatedCallback callback) {
        callback.onResult(false);
    }

    @Override
    public void onAuthenticationResult(int requestCode, int resultCode, Intent data) {
    }

    private void loadConfiguration() {
        mApplicationId = mConfigurationManager.getString(PREF_APPLICATION_ID, "");
        mControlDeviceId = mConfigurationManager.getString(PREF_CONTROL_DEVICE_ID, "");
        mSidekickDeviceId = mConfigurationManager.getString(PREF_SIDEKICK_DEVICE_ID, "");
        mEnabled = isNotEmpty(mApplicationId) && (isNotEmpty(mControlDeviceId) || isNotEmpty(mSidekickDeviceId));
        startArtikManagers();
    }

    private void startArtikManagers() {
        if (!mEnabled) {
            return;
        }
        mArtikOAuthManager = ArtikOAuthManager.newInstance(mFeatureManager.getForegroundActivity(), mApplicationId);
    }
}
