package com.vaporwarecorp.mirror.component;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
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
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.app.MirrorApplication;
import com.vaporwarecorp.mirror.component.app.LocalAssets;
import com.vaporwarecorp.mirror.util.BluetoothUtil;
import com.vaporwarecorp.mirror.util.LocationUtil;
import com.vaporwarecorp.mirror.util.NetworkUtil;
import com.vaporwarecorp.mirror.util.PropertiesUtil;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Plugin
public class AppManagerImpl extends AbstractManager implements AppManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ExitObserver mExitObserver;
    @Plug
    PluginFeatureManager mFeatureManager;

    private final MirrorApplication mApplication;

    private LocalAssets mLocalAssets;
    private Properties mProperties;
    private RefWatcher mRefWatcher;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(AppManager.class)
    public AppManagerImpl(final MirrorApplication application) {
        mApplication = application;
        mRefWatcher = LeakCanary.install(application);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AppManager ---------------------

    @Override
    public void exitApplication() {
        mExitObserver.onAppExit();
        mFeatureManager.getForegroundActivity().finish();
        Process.killProcess(Process.myPid());
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
    public String getLocalAssetPath(String assetPath) {
        return getLocalAssets().getLocalAssetPath(assetPath);
    }

    @Override
    public File getLocalAssetsDir() {
        return getLocalAssets().getLocalAssetsDir();
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
    public RefWatcher refWatcher() {
        return mRefWatcher;
    }

    @Override
    public void startActivity(final Intent intent) {
        mFeatureManager.getForegroundActivity().startActivity(intent);
    }

    private LocalAssets getLocalAssets() {
        if (mLocalAssets == null) {
            try {
                mLocalAssets = new LocalAssets(mApplication);
            } catch (IOException e) {
                Timber.e(e, "There was an error creating the LocalAssets object");
            }
        }
        return mLocalAssets;
    }
}