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
package com.vaporwarecorp.mirror.feature.main;

import android.content.Intent;
import android.text.TextUtils;
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
import com.vaporwarecorp.mirror.feature.common.presenter.YoutubePresenter;
import com.vaporwarecorp.mirror.feature.configuration.ConfigurationPresenter;
import com.vaporwarecorp.mirror.feature.google.GooglePresenter;
import com.vaporwarecorp.mirror.feature.greet.GreetPresenter;
import com.vaporwarecorp.mirror.feature.internet.InternetPresenter;
import com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter;
import com.vaporwarecorp.mirror.feature.watch.WatchCBSPresenter;
import com.vaporwarecorp.mirror.util.PermissionUtil;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import java.util.List;

import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;
import static com.vaporwarecorp.mirror.feature.common.presenter.YoutubePresenter.YOUTUBE_VIDEO_ID;
import static com.vaporwarecorp.mirror.feature.greet.GreetPresenter.GREET_TYPE;
import static com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter.TRACK_IDS;
import static java.util.Collections.singletonList;

@Plugin
@Provides(MainPresenter.class)
public class MainPresenterImpl extends AbstractFeaturePresenter<MainView> implements MainPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    CommandManager mCommandManager;
    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    MainFeature mFeature;
    @Plug
    HotWordManager mHotWordManager;
    @Plug
    SpotifyManager mSpotifyManager;
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
    public void startListening() {
        mHotWordManager.startListening();
    }

    @Override
    public void stopListening() {
        mHotWordManager.stopListening();
    }

    @Override
    public void test1() {
        mFeature.showPresenter(SpotifyPresenter.class, new Params(TRACK_IDS, singletonList("6NmXV4o6bmp704aPGyTVVG")));
    }

    @Override
    public void test2() {
        mFeature.showPresenter(YoutubePresenter.class, new Params(YOUTUBE_VIDEO_ID, "UygC613BrmE"));
    }

    @Override
    public void test3() {
        mFeature.showPresenter(GooglePresenter.class);
    }

    @Override
    public void test4() {
        mFeature.showPresenter(WatchCBSPresenter.class);
    }

    @Override
    public void test5() {
        mFeature.showPresenter(InternetPresenter.class);
    }

    @Override
    public void verifyPermissions() {
        if (PermissionUtil.checkPermissions(mView.activity()).isEmpty()) {
            startManagers();
        } else {
            mAppManager.exitApplication();
        }
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(View view) {
        super.onViewStart(view);
        checkPermissions();
    }

    @Override
    public void onViewStop(View view) {
        mSpotifyManager.stop();
        mHotWordManager.destroy();
        mCommandManager.stop();
        mEventManager.unregister(this);
        super.onViewStop(view);
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
        if (mConfigurationManager.needsInitialSetup()) {
            mFeature.showPresenter(ConfigurationPresenter.class);
        }
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
        mFeature.hidePresenter(event.getPresenterClass());
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
    public void onEvent(ForecastEvent event) {
        Timber.i("Got ForecastEvent");
        mView.setForecast(event.getForecast());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(SpeechEvent event) {
        if (!TextUtils.isEmpty(event.getMessage())) {
            mFeature.speak(event.getMessage());
        }
        startListening();
    }

    private void checkPermissions() {
        final List<String> neededPermissions = PermissionUtil.checkPermissions(mView.activity());
        if (neededPermissions.isEmpty()) {
            startManagers();
        } else {
            PermissionUtil.requestPermissions(mView.activity(), neededPermissions);
        }
    }

    private void startManagers() {
        mSpotifyManager.authenticate(mView.activity());
        mEventManager.register(this);
        mCommandManager.start();
        mHotWordManager.start();
    }
}
