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


import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionUtil {
// ------------------------------ FIELDS ------------------------------

    private static final String[] PERMISSIONS_REQUIRED = {BLUETOOTH, BLUETOOTH_ADMIN, CAMERA, INTERNET, RECORD_AUDIO,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, MODIFY_AUDIO_SETTINGS,
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
