package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.NetworkInfo.State.CONNECTED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;

public class NetworkUtil {
// -------------------------- STATIC METHODS --------------------------

    @SuppressWarnings("ResourceType")
    public static boolean isNetworkAvailable(final Context context) {
        final WifiManager manager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (manager != null) {
            if (manager.isWifiEnabled()) {
                return isNetworkConnected(context);
            } else {
                if (manager.getWifiState() == WIFI_STATE_DISABLED) {
                    manager.setWifiEnabled(true);
                }
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(final Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (manager != null) {
            final Network[] networks = manager.getAllNetworks();
            if (networks != null) {
                for (final Network network : networks) {
                    final NetworkInfo info = manager.getNetworkInfo(network);
                    if (info.getType() == TYPE_WIFI && info.getState() == CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
