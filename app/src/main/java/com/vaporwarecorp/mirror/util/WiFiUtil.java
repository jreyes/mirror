package com.vaporwarecorp.mirror.util;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WiFiUtil {
// -------------------------- STATIC METHODS --------------------------

    @SuppressWarnings("ResourceType")
    public static boolean enableWiFi(final Context context) {
        return isWiFiEnabled(context) || ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
    }

    public static boolean isWiFiEnabled(final Context context) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
    }
}
