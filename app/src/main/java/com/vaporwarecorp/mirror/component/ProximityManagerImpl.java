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
import com.radiusnetworks.proximity.*;
import com.radiusnetworks.proximity.beacon.BeaconManager;
import com.radiusnetworks.proximity.licensing.Configuration;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.event.UserInRangeEvent;
import com.vaporwarecorp.mirror.event.UserOutOfRangeEvent;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

import java.util.Collection;
import java.util.Properties;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ProximityManager.class)
public class ProximityManagerImpl extends AbstractManager implements ProximityManager, ProximityKitRangeNotifier {
// ------------------------------ FIELDS ------------------------------

    private static final int BEACON_MAX_DISTANCE = 2;
    private static final int BEACON_MAX_RETRIES = 5;
    private static final String PREF = ProximityManager.class.getName();
    private static final String PREF_API_TOKEN = PREF + ".PREF_API_TOKEN";
    private static final String PREF_GLOBAL_USER_IDENTIFIER = PREF + ".PREF_GLOBAL_USER_IDENTIFIER";
    private static final String PREF_KIT_NAME = PREF + ".PREF_KIT_NAME";
    private static final String PREF_KIT_URL = PREF + ".PREF_KIT_URL";
    private static final String PREF_USER_EMAIL = PREF + ".PREF_USER_EMAIL";
    private static final String PREF_USER_IDENTIFIER = PREF + ".PREF_USER_IDENTIFIER";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private String mApiToken;
    private int mBeaconRetries;
    private String mGlobalUserIdentifier;
    private boolean mInRange;
    private String mKitName;
    private String mKitUrl;
    private ProximityKitManager mManager;
    private String mUserEmail;
    private String mUserIdentifier;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/radiusnetworks.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("kitName", mKitName)
                .put("useIdentifier", mUserIdentifier)
                .put("userEmail", mUserEmail)
                .put("globalUserIdentifier", mGlobalUserIdentifier)
                .put("apiToken", mApiToken)
                .put("kitUrl", mKitUrl)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        stop();
        mConfigurationManager.updateString(PREF_KIT_NAME, jsonNode, "kitName");
        mConfigurationManager.updateString(PREF_USER_IDENTIFIER, jsonNode, "useIdentifier");
        mConfigurationManager.updateString(PREF_USER_EMAIL, jsonNode, "userEmail");
        mConfigurationManager.updateString(PREF_GLOBAL_USER_IDENTIFIER, jsonNode, "globalUserIdentifier");
        mConfigurationManager.updateString(PREF_API_TOKEN, jsonNode, "apiToken");
        mConfigurationManager.updateString(PREF_KIT_URL, jsonNode, "kitUrl");
        loadConfiguration();
        start();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// --------------------- Interface ProximityKitRangeNotifier ---------------------

    @Override
    public void didRangeBeaconsInRegion(Collection<ProximityKitBeacon> beacons, ProximityKitBeaconRegion region) {
        if (beacons.size() == 0) {
            mBeaconRetries++;
            if (mInRange && mBeaconRetries > BEACON_MAX_RETRIES) {
                mInRange = false;
                Timber.d("beacons.size() == 0");
                mEventManager.post(new UserOutOfRangeEvent());
            }
            return;
        }

        mBeaconRetries = 0;

        for (ProximityKitBeacon beacon : beacons) {
            if (!mInRange && beacon.getDistance() < BEACON_MAX_DISTANCE) {
                Timber.d("UserInRangeEvent - beacons.getDistance() = %s", beacon.getDistance());
                mInRange = true;
                mEventManager.post(new UserInRangeEvent());
            }
            if (mInRange && beacon.getDistance() >= BEACON_MAX_DISTANCE) {
                Timber.d("UserOutOfRangeEvent - beacons.getDistance() = %s", beacon.getDistance());
                mInRange = false;
                mEventManager.post(new UserOutOfRangeEvent());
            }
        }
    }

// --------------------- Interface ProximityManager ---------------------

    @Override
    public void start() {
        // if this manager hasn't been setup, then start the application by default
        if (StringUtils.trimToNull(mApiToken) == null) {
            mEventManager.post(new UserInRangeEvent());
            return;
        }

        // reset some variables
        mBeaconRetries = 0;
        mInRange = false;

        // create a Properties object to initialize the ProximityKitManager
        final Properties properties = new Properties();
        properties.put("PKKitName", mKitName);
        properties.put("PKUserIdentifier", mUserIdentifier);
        properties.put("PKUserEmail", mUserEmail);
        properties.put("PKGlobalUserIdentifier", mGlobalUserIdentifier);
        properties.put("PKAPIToken", mApiToken);
        properties.put("PKKitURL", mKitUrl);

        // start the ProximityKitManager
        final KitConfig kitConfig = new KitConfig(properties);
        if (mManager == null) {
            mManager = ProximityKitManager.getInstance(mAppManager.getAppContext(), kitConfig);
        } else {
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(mAppManager.getAppContext());
            beaconManager.licenseChanged(mAppManager.getAppContext());
            beaconManager.getLicenseManager().reconfigure(new Configuration(mAppManager.getAppContext(), kitConfig));
        }
        mManager.setProximityKitRangeNotifier(this);
        mManager.start();
        mManager.sync();
    }

    @Override
    public void stop() {
        if (mManager != null) {
            mManager.stop();
            mManager.setProximityKitRangeNotifier(null);
        }
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mKitName = mConfigurationManager.getString(PREF_KIT_NAME, "");
        mUserIdentifier = mConfigurationManager.getString(PREF_USER_IDENTIFIER, "");
        mUserEmail = mConfigurationManager.getString(PREF_USER_EMAIL, "");
        mGlobalUserIdentifier = mConfigurationManager.getString(PREF_GLOBAL_USER_IDENTIFIER, "");
        mApiToken = mConfigurationManager.getString(PREF_API_TOKEN, "");
        mKitUrl = mConfigurationManager.getString(PREF_KIT_URL, "");
    }
}
