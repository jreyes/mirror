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

import com.radiusnetworks.proximity.*;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.event.UserInRangeEvent;
import com.vaporwarecorp.mirror.event.UserOutOfRangeEvent;
import timber.log.Timber;

import java.util.Collection;

@Plugin
public class ProximityManagerImpl extends AbstractManager implements ProximityManager, ProximityKitRangeNotifier {
// ------------------------------ FIELDS ------------------------------

    private static final int BEACON_MAX_DISTANCE = 2;
    private static final int BEACON_MAX_RETRIES = 5;

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;

    private int mBeaconRetries;
    private boolean mInRange;
    private ProximityKitManager mManager;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(ProximityManager.class)
    public ProximityManagerImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        KitConfig kitConfig = new KitConfig(mAppManager.getApplicationProperties());
        mManager = ProximityKitManager.getInstance(mAppManager.getAppContext(), kitConfig);
        mManager.setProximityKitRangeNotifier(this);
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
    public void startProximityDetection() {
        mManager.start();
        mManager.sync();
    }

    @Override
    public void stopProximityDetection() {
        mManager.stop();
    }
}
