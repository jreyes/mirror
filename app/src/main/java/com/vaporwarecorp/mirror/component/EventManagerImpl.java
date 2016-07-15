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
package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.component.Manager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.event.Event;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

@Plugin
public class EventManagerImpl extends AbstractManager implements EventManager {
// ------------------------------ FIELDS ------------------------------

    private EventBus mEventBus;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(EventManager.class)
    public EventManagerImpl() {
        mEventBus = EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .installDefaultEventBus();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface EventManager ---------------------

    @Override
    public void post(Event event) {
        Timber.d("posting %s", event.getClass().getSimpleName());
        mEventBus.post(event);
    }

    @Override
    public void register(Manager manager) {
        mEventBus.register(manager);
    }

    @Override
    public void register(Presenter presenter) {
        mEventBus.register(presenter);
    }

    @Override
    public void register(Feature feature) {
        mEventBus.register(feature);
    }

    @Override
    public void unregister(Manager manager) {
        mEventBus.unregister(manager);
    }

    @Override
    public void unregister(Presenter presenter) {
        mEventBus.unregister(presenter);
    }

    @Override
    public void unregister(Feature feature) {
        mEventBus.unregister(feature);
    }
}
