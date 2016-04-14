package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

public class WiFiUtil {
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
    public static boolean enableWiFi(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (wifiManager.isWifiEnabled()) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.removeUpdates(LOCATION_LISTENER);
                    if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MINIMUM_TIME_BETWEEN_UPDATES,
                                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                                LOCATION_LISTENER
                        );
                    } else {
                        return true;
                    }
                }
            } else {
                wifiManager.setWifiEnabled(true);
            }
        }
        return false;
    }
}
