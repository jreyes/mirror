package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

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
        }
        return false;
    }
}
