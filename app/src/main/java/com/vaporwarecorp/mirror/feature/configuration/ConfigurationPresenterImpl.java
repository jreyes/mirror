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
package com.vaporwarecorp.mirror.feature.configuration;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PreferenceManager;
import com.vaporwarecorp.mirror.event.ResetEvent;

@Plugin
@Provides(ConfigurationPresenter.class)
public class ConfigurationPresenterImpl
        extends AbstractFeaturePresenter<ConfigurationView>
        implements ConfigurationPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PreferenceManager mPreferenceManager;
    @Plug
    ConfigurationView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurationPresenter ---------------------

    @Override
    public void dismiss() {
        mEventManager.post(new ResetEvent(ConfigurationPresenter.class));
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(ConfigurationView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mConfigurationManager.start();
    }

    @Override
    public void onViewPause(View view) {
        mConfigurationManager.stop();
        super.onViewPause(view);
    }

    @Override
    protected ConfigurationView getViewPlug() {
        return mView;
    }
}
