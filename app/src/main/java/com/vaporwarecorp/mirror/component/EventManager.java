package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.Manager;
import com.robopupu.api.feature.Feature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.event.Event;

@PlugInterface
public interface EventManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void post(Event event);

    void register(Presenter presenter);

    void register(Feature feature);

    void unregister(Presenter presenter);

    void unregister(Feature feature);
}
