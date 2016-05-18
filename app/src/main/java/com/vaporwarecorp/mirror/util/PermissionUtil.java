package com.vaporwarecorp.mirror.util;


import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtil {
// ------------------------------ FIELDS ------------------------------

    private static final String[] PERMISSIONS_REQUIRED = {BLUETOOTH, BLUETOOTH_ADMIN, CAMERA, INTERNET, RECORD_AUDIO,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_WIFI_STATE, CHANGE_WIFI_STATE,
            ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, WAKE_LOCK, RECEIVE_BOOT_COMPLETED};
    private static final int REQUEST_CODE = 31417;

// -------------------------- STATIC METHODS --------------------------

    public static List<String> checkPermissions(Activity activity) {
        List<String> neededPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS_REQUIRED) {
                if (activity.checkSelfPermission(permission) != PERMISSION_GRANTED) {
                    neededPermissions.add(permission);
                }
            }
        }
        return neededPermissions;
    }

    public static void requestPermissions(Activity activity, List<String> permissions) {
        activity.requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
    }
}
