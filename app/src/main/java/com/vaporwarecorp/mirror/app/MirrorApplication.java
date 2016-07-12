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
package com.vaporwarecorp.mirror.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.robopupu.api.app.BaseApplication;
import com.robopupu.api.app.Robopupu;
import com.robopupu.api.dependency.AppDependencyScope;
import com.robopupu.api.plugin.PluginBus;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.vaporwarecorp.mirror.app.error.MirrorAppError;
import com.vaporwarecorp.mirror.component.*;
import com.vaporwarecorp.mirror.feature.spotify.SpotifyManager;
import timber.log.Timber;

import java.io.File;
import java.io.InputStream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MirrorApplication extends BaseApplication {
// -------------------------- OTHER METHODS --------------------------

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (!"com.twilio.conversations".equals(callback.getClass().getPackage().getName())) {
            super.registerActivityLifecycleCallbacks(callback);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void configureApplication() {
        initializeApplication();
        initializeTimber();
        initializeGlide();

        MirrorAppError.setContext(getApplicationContext());
    }

    private void initializeApplication() {
        final AppDependencyScope appScope = new MirrorAppScope(this);

        new Robopupu(appScope);

        PluginBus.plug(AppManager.class);
        PluginBus.plug(PreferenceManager.class);
        PluginBus.plug(ConfigurationManager.class);
        PluginBus.plug(CommandManager.class);
        PluginBus.plug(EventManager.class);
        PluginBus.plug(ForecastManager.class);
        PluginBus.plug(HotWordManager.class);
        PluginBus.plug(ProximityManager.class);
        PluginBus.plug(SpotifyManager.class);
        PluginBus.plug(TextToSpeechManager.class);
        PluginBus.plug(TwilioManager.class);

        final PluginFeatureManager featureManager = PluginBus.plug(PluginFeatureManager.class);
        registerActivityLifecycleCallbacks(featureManager.getActivityLifecycleCallback());
    }

    private void initializeGlide() {
        Cache cache = new Cache(new File(getCacheDir(), "http"), 25 * 1024 * 1024);

        OkHttpClient mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setCache(cache);
        mOkHttpClient.setConnectTimeout(10, SECONDS);
        mOkHttpClient.setReadTimeout(10, SECONDS);
        mOkHttpClient.setWriteTimeout(10, SECONDS);

        Glide
                .get(getApplicationContext())
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mOkHttpClient));
    }

    private void initializeTimber() {
        Timber.plant(new Timber.DebugTree());
    }
}
