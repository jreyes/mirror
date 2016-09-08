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
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ApplicationEvent;
import com.vaporwarecorp.mirror.feature.main.MainView;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.event.ApplicationEvent.READY;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(MainFeature.class)
public class MainFeatureImpl extends AbstractFeature implements MainFeature {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    MainView mView;

// --------------------------- CONSTRUCTORS ---------------------------

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
        Timber.d("MainFeatureImpl.onApplicationReady");
        hideCurrentPresenter();
        mEventManager.post(new ApplicationEvent(READY));
    }

    @Override
    public void showPresenter(Class<? extends Presenter> presenterClass, Params... params) {
        if (mConfigurationManager.isPresenterEnabled(presenterClass)) {
            showView(presenterClass, false, params);
        }
    }

    @Override
    protected void onStart() {
        Timber.d("MainFeatureImpl.onStart");
        super.onStart();
        //showPresenter(SplashPresenter.class);
    }
}
