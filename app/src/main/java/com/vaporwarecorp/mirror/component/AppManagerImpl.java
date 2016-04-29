package com.vaporwarecorp.mirror.component;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.AppToolkit;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.app.MirrorApplication;
import com.vaporwarecorp.mirror.util.BluetoothUtil;
import com.vaporwarecorp.mirror.util.LocationUtil;
import com.vaporwarecorp.mirror.util.NetworkUtil;
import com.vaporwarecorp.mirror.util.PropertiesUtil;

import java.io.File;
import java.util.Properties;

@Plugin
public class AppManagerImpl extends AbstractManager implements AppManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ExitObserver mExitObserver;
    @Plug
    PluginFeatureManager mFeatureManager;

    private final MirrorApplication mApplication;

    private Properties mProperties;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(AppManager.class)
    public AppManagerImpl(final MirrorApplication application) {
        mApplication = application;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AppManager ---------------------

    @Override
    public void exitApplication() {
        mExitObserver.onAppExit();
        mFeatureManager.getForegroundActivity().finish();
    }

    @Override
    public Context getAppContext() {
        return mApplication.getApplicationContext();
    }

    @Override
    public MirrorApplication getApplication() {
        return mApplication;
    }

    @Override
    public File getApplicationDirectory() {
        return AppToolkit.getApplicationDirectory(getAppContext());
    }

    @Override
    public String getApplicationDirectoryPath() {
        return AppToolkit.getApplicationDirectoryPath(getAppContext());
    }

    @Override
    public Properties getApplicationProperties() {
        if (mProperties == null) {
            mProperties = PropertiesUtil.loadProperties(getAppContext());
        }
        return mProperties;
    }

    @Override
    @ColorInt
    public int getColor(@ColorRes int colorResId) {
        return ContextCompat.getColor(mApplication, colorResId);
    }

    @Override
    public int getInteger(@IntegerRes int intResId) {
        return mApplication.getResources().getInteger(intResId);
    }

    @Override
    public String getPackageName() {
        return getAppContext().getPackageName();
    }

    @Override
    public String getString(final @StringRes int stringResId, final Object... formatArgs) {
        return mApplication.getString(stringResId, formatArgs);
    }

    @Override
    public boolean isBluetoothAvailable() {
        return BluetoothUtil.isBluetoothAvailable();
    }

    @Override
    public boolean isLocationAvailable() {
        return LocationUtil.isLocationAvailable(mApplication.getApplicationContext());
    }

    @Override
    public boolean isNetworkAvailable() {
        return NetworkUtil.isNetworkAvailable(mApplication.getApplicationContext());
    }

    @Override
    public void startActivity(final Intent intent) {
        mFeatureManager.getForegroundActivity().startActivity(intent);
    }
}