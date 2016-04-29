package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
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
        Timber.d("posting " + event.getClass().getSimpleName());
        mEventBus.post(event);
    }

    @Override
    public void register(Presenter presenter) {
        mEventBus.register(presenter);
    }

    @Override
    public void unregister(Presenter presenter) {
        mEventBus.unregister(presenter);
    }
}
