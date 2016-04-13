package com.vaporwarecorp.mirror;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.vaporwarecorp.mirror.command.internet.InternetCommand;
import com.vaporwarecorp.mirror.command.map.ShowDirectionsCommand;
import com.vaporwarecorp.mirror.command.map.ShowMapCommand;
import com.vaporwarecorp.mirror.command.mikutime.MikuTimeCommand;
import com.vaporwarecorp.mirror.command.music.MusicChartsCommand;
import com.vaporwarecorp.mirror.command.music.MusicSearchCommand;
import com.vaporwarecorp.mirror.command.nomatch.NoMatchCommand;
import com.vaporwarecorp.mirror.manager.*;
import com.vaporwarecorp.mirror.util.BluetoothUtil;
import com.vaporwarecorp.mirror.util.WiFiUtil;
import com.vaporwarecorp.mirror.vendor.ijk.IjkVideoView;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MirrorApp extends MultiDexApplication {
// ------------------------------ FIELDS ------------------------------

    private static final long CACHE_SIZE = 25 * 1024 * 1024;
    private static final Object LOCK = new Object();

    private ForecastManager mForecastManager;
    private HandWaveManager mHandWaveManager;
    private HotWordManager mHotWordManager;
    private HoundifyManager mHoundifyManager;
    private LocalAssetManager mLocalAssetManager;
    private OkHttpClient mOkHttpClient;
    private Properties mProperties;
    private ProximityManager mProximityManager;
    private RefWatcher mRefWatcher;
    private SoundManager mSoundManager;
    private SpotifyManager mSpotifyManager;
    private TextToSpeechManager mTextToSpeechManager;
    private VideoManager mVideoManager;

// -------------------------- STATIC METHODS --------------------------

    public static ForecastManager forecast(@NonNull Context context) {
        return getApplication(context).mForecastManager;
    }

    public static MirrorApp getApplication(@NonNull Context context) {
        return (MirrorApp) context.getApplicationContext();
    }

    public static HandWaveManager handWave(@NonNull Context context) {
        return getApplication(context).mHandWaveManager;
    }

    public static HotWordManager hotWord(@NonNull Context context) {
        return getApplication(context).mHotWordManager;
    }

    public static HoundifyManager houndify(@NonNull Context context) {
        return getApplication(context).mHoundifyManager;
    }

    public static LocalAssetManager localAsset(@NonNull Context context) {
        return getApplication(context).mLocalAssetManager;
    }

    public static ProximityManager proximity(@NonNull Context context) {
        return getApplication(context).mProximityManager;
    }

    public static RefWatcher refWatcher(@NonNull Context context) {
        return getApplication(context).mRefWatcher;
    }

    public static SoundManager sound(@NonNull Context context) {
        return getApplication(context).mSoundManager;
    }

    public static SpotifyManager spotify(@NonNull Context context) {
        return getApplication(context).mSpotifyManager;
    }

    public static TextToSpeechManager textToSpeech(@NonNull Context context) {
        return getApplication(context).mTextToSpeechManager;
    }

    public static VideoManager video(@NonNull Context context, @NonNull IjkVideoView view) {
        VideoManager manager = getApplication(context).mVideoManager;
        manager.setIjkVideoView(view);
        return manager;
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean initializeManagers() {
        synchronized (LOCK) {
            boolean result = BluetoothUtil.enableBluetooth() && WiFiUtil.enableWiFi(this) && initForecastManager() &&
                    initProximityManager() && initSoundManager() && initHotWordManager() && initHoundifyManager() &&
                    initHandWaveManager() && initVideoManager() && initSpotifyManager();
            if (result) {
                initHoundifyCommands();
            }
            return result;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeLibraries();
        initializeManagers();
    }

    private void initEventBus() {
        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
    }

    private boolean initForecastManager() {
        if (mForecastManager == null) {
            mForecastManager = new ForecastManager(this, mProperties);
        }
        return true;
    }

    private void initGlide() {
        Glide
                .get(getApplicationContext())
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mOkHttpClient));
    }

    private boolean initHandWaveManager() {
        if (mHandWaveManager == null) {
            mHandWaveManager = new HandWaveManager(this);
        }
        return true;
    }

    private boolean initHotWordManager() {
        if (mHotWordManager == null) {
            try {
                mHotWordManager = new HotWordManager(this, mProperties);
            } catch (IOException e) {
                Timber.e(e, "Error init HotWordManager");
                return false;
            }
        }
        return true;
    }

    private void initHoundifyCommands() {
        new InternetCommand().registerHoundify(mHoundifyManager);
        new ShowDirectionsCommand().registerHoundify(mHoundifyManager);
        new ShowMapCommand().registerHoundify(mHoundifyManager);
        new MikuTimeCommand().registerHoundify(mHoundifyManager);
        new MusicChartsCommand().registerHoundify(mHoundifyManager);
        new MusicSearchCommand().registerHoundify(mHoundifyManager);
        new NoMatchCommand().registerHoundify(mHoundifyManager);
    }

    private boolean initHoundifyManager() {
        if (mHoundifyManager == null) {
            mHoundifyManager = new HoundifyManager(this, mProperties);
        }
        return true;
    }

    private void initLeakCanary() {
        mRefWatcher = LeakCanary.install(this);
    }

    private void initLocalAssetManager() {
        try {
            mLocalAssetManager = new LocalAssetManager(this);
        } catch (IOException e) {
            Timber.e(e, "Error init LocalAssetManager");
        }
    }

    private void initOkClient() {
        Cache cache = new Cache(new File(getCacheDir(), "http"), CACHE_SIZE);

        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setCache(cache);
        mOkHttpClient.setConnectTimeout(10, SECONDS);
        mOkHttpClient.setReadTimeout(10, SECONDS);
        mOkHttpClient.setWriteTimeout(10, SECONDS);
    }

    private void initProperties() {
        try {
            mProperties = new Properties();
            mProperties.load(getAssets().open("mirror.properties"));
        } catch (IOException e) {
            Timber.e(e, "Error init Properties");
            mProperties = null;
        }
    }

    private boolean initProximityManager() {
        if (mProximityManager == null) {
            mProximityManager = new ProximityManager(this, mProperties);
        }
        return true;
    }

    private boolean initSoundManager() {
        if (mSoundManager == null) {
            mSoundManager = new SoundManager(getApplicationContext());
        }
        return true;
    }

    private boolean initSpotifyManager() {
        if (mSpotifyManager == null) {
            mSpotifyManager = new SpotifyManager(this, mProperties);
        }
        return true;
    }

    private void initTextToSpeech() {
        mTextToSpeechManager = new TextToSpeechManager(this);
    }

    private void initTimber() {
        Timber.plant(new Timber.DebugTree());
    }

    private boolean initVideoManager() {
        if (mVideoManager == null) {
            mVideoManager = new VideoManager();
        }
        return true;
    }

    private void initializeLibraries() {
        synchronized (LOCK) {
            initLeakCanary();
            initTimber();
            initProperties();
            initLocalAssetManager();
            initEventBus();
            initOkClient();
            initGlide();
            initTextToSpeech();
        }
    }
}
