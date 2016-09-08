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
package com.vaporwarecorp.mirror.feature.splash;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.feature.MainFeature;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.util.RxUtil.delay;

@Plugin
@Provides(SplashPresenter.class)
public class SplashPresenterImpl extends AbstractFeaturePresenter<SplashView> implements SplashPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    MainFeature mFeature;
    @Plug
    SplashManager mManager;
    @Plug
    SplashView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(SplashView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewStart(final View view) {
        super.onViewStart(view);
        mView.setPictureUrl(mManager.getRandomSplash());
        isApplicationReady();
    }

    @Override
    protected SplashView getViewPlug() {
        return mView;
    }

    private void isApplicationReady() {
        delay(l -> {
            Timber.d("mAppManager.isBluetoothAvailable() %s", mAppManager.isBluetoothAvailable());
            Timber.d("mAppManager.isNetworkAvailable() %s", mAppManager.isNetworkAvailable());
            Timber.d("mAppManager.isLocationAvailable() %s", mAppManager.isLocationAvailable());
            if (mAppManager.isBluetoothAvailable() &&
                    mAppManager.isNetworkAvailable() &&
                    mAppManager.isLocationAvailable()) {
                mFeature.onApplicationReady();
            } else {
                isApplicationReady();
            }
        }, 10);
    }
}
