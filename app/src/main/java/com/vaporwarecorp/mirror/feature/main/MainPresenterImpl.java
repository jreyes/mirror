package com.vaporwarecorp.mirror.feature.main;

import android.content.Intent;
import com.hound.android.fd.Houndify;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.component.*;
import com.vaporwarecorp.mirror.event.*;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.greet.GreetPresenter;
import com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import java.util.ArrayList;

import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;
import static com.vaporwarecorp.mirror.feature.greet.GreetPresenter.GREET_TYPE;
import static com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter.TRACK_IDS;

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
    SpotifyManager mSpotifyManager;
    @Plug
    TextToSpeechManager mTextToSpeechManager;
    @Plug
    MainView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MainPresenter ---------------------

    @Override
    public void onViewResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Houndify.REQUEST_CODE) {
            processCommand(resultCode, data);
        } else if (requestCode == SpotifyManager.REQUEST_CODE) {
            mSpotifyManager.processAuthentication(resultCode, data);
        }
    }

    @Override
    public void processCommand(int resultCode, Intent data) {
        mCommandManager.processCommand(resultCode, data);
    }

    @Override
    public void speak(String textToSpeak) {
        if (StringUtils.isNoneEmpty(textToSpeak)) {
            mTextToSpeechManager.speak(textToSpeak);
        }
        startListening();
    }

    @Override
    public void startListening() {
        mHotWordManager.startListening();
    }

    public void startSpotify() {
        ArrayList<String> trackIds = new ArrayList<>();
        trackIds.add("spotify:track:1SscHHP2wlcyLI3ikIHDr9");
        trackIds.add("spotify:track:3wIOZOjOle5KyCqcYJNnOH");
        trackIds.add("spotify:track:4PS0VSN9j8za2aU0Ac14lt");
        mFeature.showPresenter(SpotifyPresenter.class, new Params(TRACK_IDS, trackIds));
    }

    @Override
    public void stopListening() {
        mHotWordManager.stopListening();
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(View view) {
        super.onViewStart(view);
        mSpotifyManager.authenticate(mView.activity());
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
        stopListening();
        mCommandManager.voiceSearch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(ResetEvent event) {
        mFeature.hideCurrentPresenter();
        startListening();
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(ForecastEvent event) {
        Timber.i("Got ForecastEvent");
        mView.setForecast(event.getForecast());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventManager.register(this);
        mCommandManager.start();
    }

    @Override
    protected void onStop() {
        mSpotifyManager.stop();
        mTextToSpeechManager.destroy();
        mHotWordManager.destroy();
        mCommandManager.stop();
        mEventManager.unregister(this);
        super.onStop();
    }
}
