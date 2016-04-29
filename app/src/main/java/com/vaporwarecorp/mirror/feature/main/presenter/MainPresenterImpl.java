package com.vaporwarecorp.mirror.feature.main.presenter;

import android.content.Intent;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.component.CommandManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.HotWordManager;
import com.vaporwarecorp.mirror.component.TextToSpeechManager;
import com.vaporwarecorp.mirror.event.*;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.greet.presenter.GreetPresenter;
import com.vaporwarecorp.mirror.feature.main.view.MainView;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.event.CommandEvent.TYPE_COMMAND_SUCCESS;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;
import static com.vaporwarecorp.mirror.feature.greet.presenter.GreetPresenter.GREET_TYPE;

@Plugin
@Provides(MainPresenter.class)
public class MainPresenterImpl extends AbstractFeaturePresenter<MainView> implements MainPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    CommandManager mCommandManager;
    @Plug
    EventManager mEventManager;
    @Plug
    MainFeature mFeature;
    @Plug
    HotWordManager mHotWordManager;
    @Plug
    TextToSpeechManager mTextToSpeechManager;
    @Plug
    MainView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MainPresenter ---------------------

    @Override
    public void processCommand(int resultCode, Intent data) {
        mCommandManager.processCommand(resultCode, data);
    }

    public void speak(String textToSpeak) {
        if (StringUtils.isNoneEmpty(textToSpeak)) {
            mTextToSpeechManager.speak(textToSpeak);
        }
    }

    @Override
    public void startListening() {
        mHotWordManager.startListening();
    }

    @Override
    public void stopListening() {
        mHotWordManager.stopListening();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public MainView getViewPlug() {
        return mView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(UserInRangeEvent event) {
        mFeature.showPresenter(GreetPresenter.class, new Params(GREET_TYPE, TYPE_WELCOME));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(GreetEvent event) {
        if (TYPE_WELCOME.equals(event.getType())) {
            mFeature.displayView();
            mHotWordManager.startListening();
        } else {
            mHotWordManager.stopListening();
            mFeature.hideCurrentPresenter();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HotWordEvent event) {
        mCommandManager.voiceSearch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(UserOutOfRangeEvent event) {
        mFeature.hideView();
        mFeature.showPresenter(GreetPresenter.class, new Params(GREET_TYPE, TYPE_GOODBYE));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpeechEvent event) {
        speak(event.getMessage());
        startListening();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(ForecastEvent event) {
        Timber.i("Got ForecastEvent");
        mView.setForecast(event.getForecast());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(CommandEvent event) {
        if (TYPE_COMMAND_SUCCESS.equals(event.getType())) {
            mFeature.hideCurrentPresenter();
        }
        speak(event.getMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventManager.register(this);
    }

    @Override
    protected void onStop() {
        mEventManager.unregister(this);
        super.onStop();
    }
}
