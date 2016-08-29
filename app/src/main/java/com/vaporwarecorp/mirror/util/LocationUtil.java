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
package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import timber.log.Timber;

import static android.content.Context.LOCATION_SERVICE;

public class LocationUtil {
// ------------------------------ FIELDS ------------------------------

    private static final LocationListener LOCATION_LISTENER = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 60000;

// -------------------------- STATIC METHODS --------------------------

    @SuppressWarnings("ResourceType")
    public static Location getLastKnownLocation(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @SuppressWarnings("ResourceType")
    public static boolean isLocationAvailable(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Timber.d("NETWORK_PROVIDER is enabled");
            manager.removeUpdates(LOCATION_LISTENER);
            if (manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null) {
                manager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MINIMUM_TIME_BETWEEN_UPDATES,
                        MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                        LOCATION_LISTENER
                );
            } else {
                return true;
            }
        } else {
            Timber.e("NETWORK_PROVIDER is disabled");
        }
        return false;
    }
}
