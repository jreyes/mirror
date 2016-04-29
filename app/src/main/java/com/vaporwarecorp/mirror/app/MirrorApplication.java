package com.vaporwarecorp.mirror.app;

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
import timber.log.Timber;

import java.io.File;
import java.io.InputStream;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MirrorApplication extends BaseApplication {
// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        initializeTimber();
        initializeGlide();

        MirrorAppError.setContext(getApplicationContext());
    }

    @Override
    protected void configureApplication() {
        final AppDependencyScope appScope = new MirrorAppScope(this);

        new Robopupu(appScope);

        PluginBus.plug(AppManager.class);
        PluginBus.plug(CommandManager.class);
        PluginBus.plug(EventManager.class);
        PluginBus.plug(ForecastManager.class);
        PluginBus.plug(HotWordManager.class);
        PluginBus.plug(PreferenceManager.class);
        PluginBus.plug(ProximityManager.class);
        PluginBus.plug(TextToSpeechManager.class);

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
