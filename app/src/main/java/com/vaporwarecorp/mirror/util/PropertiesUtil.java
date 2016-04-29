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
