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
package com.vaporwarecorp.mirror.feature.configurable;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.feature.common.presenter.AbstractMirrorFeaturePresenter;
import timber.log.Timber;

@Plugin
@Provides(ConfigurablePresenter.class)
public class ConfigurablePresenterImpl
        extends AbstractMirrorFeaturePresenter<ConfigurableView>
        implements ConfigurablePresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    ConfigurableFeature mFeature;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug
    ConfigurationManager mManager;
    @Plug
    ConfigurableView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurablePresenter ---------------------

    @Override
    public void dismiss() {
        mAppManager.startMainFeature();
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mManager.start();
    }

    @Override
    public void onViewPause(View view) {
        mManager.stop();
        super.onViewPause(view);
    }

    @Override
    public void onViewStop(View view) {
        Timber.d("onViewStop(View)");
        mFeatureManager.stopFeature(mFeature);
        super.onViewStop(view);
    }

    @Override
    protected ConfigurableView getViewPlug() {
        return mView;
    }
}
