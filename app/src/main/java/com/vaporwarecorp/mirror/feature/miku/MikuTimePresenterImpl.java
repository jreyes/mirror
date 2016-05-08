package com.vaporwarecorp.mirror.feature.miku;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;

import java.util.Random;

@Plugin
public class MikuTimePresenterImpl extends AbstractFeaturePresenter<MikuTimeView> implements MikuTimePresenter {
// ------------------------------ FIELDS ------------------------------

    private static final String[] MIKU_VIDEO_IDS = {"u99kOUA5EpE", "UygC613BrmE", "TXwW5ZuKlwE", "DHioZY5CdYw"};

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    MikuTimeView mView;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(MikuTimePresenter.class)
    public MikuTimePresenterImpl() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(MikuTimeView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);

        String youtubeVideoId = MIKU_VIDEO_IDS[new Random().nextInt(MIKU_VIDEO_IDS.length)];
        mView.setYoutubeVideo(youtubeVideoId, () -> mEventManager.post(new ResetEvent()));
    }

    @Override
    protected MikuTimeView getViewPlug() {
        return mView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }
}
