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
package com.vaporwarecorp.mirror.feature.radiusnetworks;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.radiusnetworks.proximity.KitConfig;
import com.radiusnetworks.proximity.ProximityKitBeacon;
import com.radiusnetworks.proximity.ProximityKitBeaconRegion;
import com.radiusnetworks.proximity.ProximityKitManager;
import com.radiusnetworks.proximity.ProximityKitRangeNotifier;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ProximityEvent;
import com.vaporwarecorp.mirror.event.UserInRangeEvent;
import com.vaporwarecorp.mirror.event.UserOutOfRangeEvent;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.vaporwarecorp.mirror.util.JsonUtil.createTextNode;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(ProximityManager.class)
public class ProximityManagerImpl extends AbstractMirrorManager implements ProximityManager, ProximityKitRangeNotifier {
// ------------------------------ FIELDS ------------------------------

    private static final double BEACON_MAX_DISTANCE = 8.0;
    private static final int BEACON_MAX_RETRIES = 10;
    private static final double BEACON_SHARE_DISTANCE = 1.85;
    private static final String PREF = ProximityManager.class.getName();
    private static final String PREF_API_TOKEN = PREF + ".PREF_API_TOKEN";
    private static final String PREF_KIT_URL = PREF + ".PREF_KIT_URL";

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;

    private String mApiToken;
    private int mBeaconRetries;
    private boolean mInRange;
    private boolean mInSharingRange;
    private String mKitUrl;
    private ProximityKitManager mManager;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/radiusnetworks.json";
    }

    @Override
    public String getJsonValues() {
        return createTextNode("apiToken", mApiToken)
                .put("kitUrl", mKitUrl)
                .toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateString(PREF_API_TOKEN, jsonNode, "apiToken");
        mConfigurationManager.updateString(PREF_KIT_URL, jsonNode, "kitUrl");
        loadConfiguration();
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeatureStart() {
        Timber.d("onFeatureStart");

        // if this manager hasn't been setup, then start the application by default
        if (isEmpty(mApiToken) || isEmpty(mKitUrl)) {
            mEventManager.post(new UserInRangeEvent());
            return;
        }

        // reset some variables
        mBeaconRetries = 0;
        mInRange = false;
        mInSharingRange = false;

        // create a Properties object to initialize the ProximityKitManager
        final Map<String, String> settings = new HashMap<>();
        settings.put(KitConfig.CONFIG_API_URL, mKitUrl);
        settings.put(KitConfig.CONFIG_API_TOKEN, mApiToken);

        // start the ProximityKitManager
        if (mManager == null) {
            final KitConfig kitConfig = new KitConfig(settings);
            mManager = ProximityKitManager.getInstance(mAppManager.getAppContext(), kitConfig);
            mManager.getBeaconManager().setForegroundScanPeriod(2000L);
            mManager.sync();

            RangedBeacon.setSampleExpirationMilliseconds(2000L);
        }

        mManager.setProximityKitRangeNotifier(this);
        mManager.start();
            /*
        } else {
            beaconManager.licenseChanged(mAppManager.getAppContext());
            beaconManager.getLicenseManager().reconfigure(new Configuration(mAppManager.getAppContext(), kitConfig));
        }
        */
    }

    @Override
    public void onFeatureStop() {
        Timber.d("onFeatureStop");

        // if the manager is null then don't do anything
        if (mManager == null) {
            return;
        }

        // stop the ProximityManager
        mManager.stop();
        mManager.setProximityKitRangeNotifier(null);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

// --------------------- Interface ProximityKitRangeNotifier ---------------------

    @Override
    public void didRangeBeaconsInRegion(@NonNull Collection<ProximityKitBeacon> beacons,
                                        @NonNull ProximityKitBeaconRegion region) {
        if (beacons.size() == 0) {
            if (mInRange && mBeaconRetries > BEACON_MAX_RETRIES) {
                Timber.d("beacons.size() == 0");
                mInRange = false;
                mInSharingRange = false;
                mEventManager.post(new UserOutOfRangeEvent());
            }
            mBeaconRetries++;
            return;
        }

        mBeaconRetries = 0;

        for (ProximityKitBeacon beacon : beacons) {
            double distance = beacon.getDistance();
            if (!mInRange && distance < BEACON_MAX_DISTANCE) {
                Timber.d("UserInRangeEvent - beacons.getDistance() = %s", distance);
                mInRange = true;
                mEventManager.post(new UserInRangeEvent());
            }
            if (mInRange) {
                if (!mInSharingRange && distance < BEACON_SHARE_DISTANCE) {
                    Timber.d("BEACON_SHARE_DISTANCE - in distance");
                    mInSharingRange = true;
                    mEventManager.post(new ProximityEvent(ProximityEvent.SHARE_START));
                }
                if (mInSharingRange && distance >= BEACON_SHARE_DISTANCE) {
                    Timber.d("BEACON_SHARE_DISTANCE - out distance");
                    mInSharingRange = false;
                }
            }
            if (mInRange && distance >= BEACON_MAX_DISTANCE) {
                if (mBeaconRetries > BEACON_MAX_RETRIES) {
                    Timber.d("UserOutOfRangeEvent - beacons.getDistance() = %s", distance);
                    mInRange = false;
                    mInSharingRange = false;
                    mEventManager.post(new UserOutOfRangeEvent());
                }
                mBeaconRetries++;
            }
        }
    }

    /**
     * Load the configuration of the component.
     */
    private void loadConfiguration() {
        mApiToken = mConfigurationManager.getString(PREF_API_TOKEN, "");
        mKitUrl = mConfigurationManager.getString(PREF_KIT_URL, "");
    }
}
