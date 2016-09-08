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
package com.vaporwarecorp.mirror.component;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;
import com.squareup.okhttp.OkHttpClient;
import com.vaporwarecorp.mirror.app.MirrorApplication;

import java.io.File;

@PlugInterface
public interface AppManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    /**
     * Cancel a pending intent
     */
    void cancelPendingIntent(Intent intent);

    /**
     * Exists the application.
     */
    void exitApplication();

    /**
     * Gets the application {@link Context}.
     *
     * @return A @link Context}.
     */
    Context getAppContext();

    /**
     * Gets the application.
     *
     * @return The application as @link RobopupuApplication}.
     */
    MirrorApplication getApplication();

    /**
     * Gets application directory.
     *
     * @return The directory as a {@link File}. If accessing the directory fails, {@code null} is
     * returned.
     */
    File getApplicationDirectory();

    /**
     * Gets application directory path.
     *
     * @return The directory path as a {@link File}. If accessing the directory fails, {@code null}
     * is returned.
     */
    String getApplicationDirectoryPath();

    /**
     * Gets the specified color value.
     *
     * @param colorResId A color resource id.
     * @return The int value for the color.
     */
    @ColorInt
    int getColor(@ColorRes int colorResId);

    /**
     * Gets the specified integer ressource.
     *
     * @param intResId The resource id of the integer.
     * @return An {@code int} value.
     */
    int getInteger(@IntegerRes int intResId);

    /**
     * Returns a physical location of an asset.
     *
     * @param assetPath asset path location.
     * @return physical location of the asset.
     */
    String getLocalAssetPath(String assetPath);

    /**
     * Return the LocalAssets file object.
     *
     * @return Local assets file.
     */
    File getLocalAssetsDir();

    String getPackageName();

    /**
     * Gets the specified string resource formatted with the given optional arguments.
     *
     * @param stringResId The resource id of the string.
     * @param formatArgs  Optional formatting arguments.
     * @return A {@link String}.
     */
    String getString(@StringRes int stringResId, final Object... formatArgs);

    boolean isBluetoothAvailable();

    boolean isLocationAvailable();

    /**
     * Tests if network is available.
     *
     * @return A {@code boolean} value.
     */
    boolean isNetworkAvailable();

    /**
     * Returns an OkHttpClient instance
     */
    OkHttpClient okHttpClient();

    /**
     * Start a service.
     */
    void startService(Class serviceClass, String action);
}