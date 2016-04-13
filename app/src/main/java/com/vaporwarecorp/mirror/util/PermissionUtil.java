package com.vaporwarecorp.mirror.util;

import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtil {
// ------------------------------ FIELDS ------------------------------

    private static final int PERMISSIONS_REQUEST = 1;
    private static final String[] PERMISSIONS_REQUIRED = {BLUETOOTH, BLUETOOTH_ADMIN, CAMERA, INTERNET, RECORD_AUDIO,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, MOUNT_UNMOUNT_FILESYSTEMS, ACCESS_WIFI_STATE,
            CHANGE_WIFI_STATE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, WAKE_LOCK, WRITE_SETTINGS,
            RECEIVE_BOOT_COMPLETED};

// -------------------------- STATIC METHODS --------------------------

    public static void checkPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> neededPermissions = new ArrayList<>();
            for (String permission : PERMISSIONS_REQUIRED) {
                if (activity.checkSelfPermission(permission) != PERMISSION_GRANTED) {
                    neededPermissions.add(permission);
                }
            }
            if (!neededPermissions.isEmpty()) {
                activity.requestPermissions(neededPermissions.toArray(new String[neededPermissions.size()]), PERMISSIONS_REQUEST);
            }
        }
    }
}
