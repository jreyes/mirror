package com.vaporwarecorp.mirror.util;

import android.bluetooth.BluetoothAdapter;

public class BluetoothUtil {
// -------------------------- STATIC METHODS --------------------------

    public static boolean isBluetoothAvailable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
                adapter.enable();
            }
        }
        return adapter.getState() == BluetoothAdapter.STATE_ON;
    }
}
