package com.vaporwarecorp.mirror.component;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.app.MirrorApplication;

import java.io.File;
import java.util.Properties;

@PlugInterface
public interface AppManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

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

    Properties getApplicationProperties();

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
     * Starts the specified {@link Activity}.
     *
     * @param intent An {@link Intent} specifying the {@link Activity} to be started.
     */
    void startActivity(Intent intent);
}