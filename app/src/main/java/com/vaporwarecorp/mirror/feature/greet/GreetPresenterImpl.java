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
package com.vaporwarecorp.mirror.feature.greet;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.Event;
import com.vaporwarecorp.mirror.event.GreetEvent;
import com.vaporwarecorp.mirror.util.RxUtil;

import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;

@Plugin
@Provides(GreetPresenter.class)
public class GreetPresenterImpl extends AbstractFeaturePresenter<GreetView> implements GreetPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    GreetManager mManager;
    @Plug
    GreetView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface GreetPresenter ---------------------

    @Override
    public void onAnimationEnd() {
        RxUtil.delay(l -> mEventManager.post(getGreetEvent()), 5);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(GreetView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(final View view) {
        super.onViewResume(view);
        mView.displayGreet(getGreeting());
    }

    @Override
    protected GreetView getViewPlug() {
        return mView;
    }

    private Event getGreetEvent() {
        return new GreetEvent(isWelcome() ? TYPE_WELCOME : TYPE_GOODBYE);
    }

    private String getGreeting() {
        return isWelcome() ? mManager.welcome() : mManager.goodbye();
    }

    private boolean isWelcome() {
        return getParams().containsValue(TYPE_WELCOME);
    }
}
