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
        hidePresenter(mView.getMainPresenterClass());
    }

    @Override
    public void hidePresenter(Class<? extends Presenter> presenterClass) {
        if (presenterClass == null) {
            return;
        }
        hideView(presenterClass, false, null);
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
        showView(presenterClass, false, params);
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
        mTextToSpeechManager.stop();
        mProximityManager.stopProximityDetection();
        mEventManager.unregister(this);
        super.onStop();
    }
}
