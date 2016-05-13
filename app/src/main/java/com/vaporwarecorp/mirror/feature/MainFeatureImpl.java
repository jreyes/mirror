package com.vaporwarecorp.mirror.feature;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.feature.AbstractFeature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.*;
import com.vaporwarecorp.mirror.event.ApplicationEvent;
import com.vaporwarecorp.mirror.event.CommandEvent;
import com.vaporwarecorp.mirror.feature.main.MainView;
import com.vaporwarecorp.mirror.feature.splash.SplashPresenter;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.vaporwarecorp.mirror.event.ApplicationEvent.READY;
import static com.vaporwarecorp.mirror.event.CommandEvent.TYPE_COMMAND_SUCCESS;

@Plugin
public class MainFeatureImpl extends AbstractFeature implements MainFeature {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug
    ForecastManager mForecastManager;
    @Plug
    ProximityManager mProximityManager;
    @Plug
    TextToSpeechManager mTextToSpeechManager;
    @Plug
    MainView mView;

    private Class<? extends Presenter> mCurrentPresenterClass;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(MainFeature.class)
    public MainFeatureImpl() {
        super(MainScope.class, true);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MainFeature ---------------------

    @Override
    public void displayView() {
        hideCurrentPresenter();
        mView.displayView();
    }

    @Override
    public void hideCurrentPresenter() {
        if (mCurrentPresenterClass == null) {
            return;
        }

        hideView(mCurrentPresenterClass, false, null);
        mCurrentPresenterClass = null;
    }

    @Override
    public void hideView() {
        mView.hideView();
    }

    @Override
    public void onApplicationReady() {
        hideCurrentPresenter();
        mProximityManager.startProximityDetection();
        mForecastManager.startReceiver();
        mEventManager.post(new ApplicationEvent(READY));
    }

    @Override
    public void showPresenter(Class<? extends Presenter> presenterClass, Params... params) {
        mCurrentPresenterClass = presenterClass;
        showView(mCurrentPresenterClass, false, params);
    }

    @Override
    public void speak(String textToSpeak) {
        if (StringUtils.isNoneEmpty(textToSpeak)) {
            mTextToSpeechManager.speak(textToSpeak);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(CommandEvent event) {
        speak(event.getMessage());
        if (TYPE_COMMAND_SUCCESS.equals(event.getType())) {
            hideCurrentPresenter();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventManager.register(this);
        showPresenter(SplashPresenter.class);
    }

    @Override
    protected void onStop() {
        mProximityManager.stopProximityDetection();
        mEventManager.unregister(this);
        super.onStop();
    }
}
