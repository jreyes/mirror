package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface HotWordManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void destroy();

    void start();

    void startListening();

    void stopListening();
}
