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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.squareup.okhttp.OkHttpClient;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.app.MirrorApplication;
import com.vaporwarecorp.mirror.component.app.LocalAssets;
import com.vaporwarecorp.mirror.feature.configurable.ConfigurableActivity;
import com.vaporwarecorp.mirror.feature.main.MirrorActivity;
import com.vaporwarecorp.mirror.util.BluetoothUtil;
import com.vaporwarecorp.mirror.util.LocationUtil;
import com.vaporwarecorp.mirror.util.NetworkUtil;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static android.content.Context.ALARM_SERVICE;

@Plugin
public class AppManagerImpl extends AbstractManager implements AppManager {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ExitObserver mExitObserver;
    @Plug
    PluginFeatureManager mFeatureManager;

    private final MirrorApplication mApplication;
    private LocalAssets mLocalAssets;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(AppManager.class)
    public AppManagerImpl(final MirrorApplication application) {
        mApplication = application;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AppManager ---------------------

    @Override
    public void cancelPendingIntent(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mApplication, 0, intent, 0);
        ((AlarmManager) mApplication.getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
        pendingIntent.cancel();
    }

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
    public OkHttpClient okHttpClient() {
        return mApplication.okHttpClient();
    }

    @Override
    public void startActivity(final Intent intent) {
        Activity activity = mFeatureManager.getForegroundActivity();
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void startConfigurableFeature() {
        Intent i = new Intent(getAppContext(), ConfigurableActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void startMainFeature() {
        Intent i = new Intent(getAppContext(), MirrorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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