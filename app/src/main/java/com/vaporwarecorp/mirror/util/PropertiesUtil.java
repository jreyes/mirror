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
import timber.log.Timber;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
// ------------------------------ FIELDS ------------------------------

    private static final String PROPERTIES_FILE_NAME = "mirror.properties";

// -------------------------- STATIC METHODS --------------------------

    public static Properties loadProperties(final Context context) {
        try {
            Properties properties = new Properties();
            properties.load(context.getAssets().open(PROPERTIES_FILE_NAME));
            return properties;
        } catch (IOException e) {
            Timber.e(e, "Error init Properties");
            return null;
        }
    }
}
