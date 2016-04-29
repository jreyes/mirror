package com.vaporwarecorp.mirror.component;

import com.robopupu.api.plugin.PlugInterface;
import com.robopupu.api.plugin.PlugMode;

@PlugInterface(PlugMode.BROADCAST)
public interface ExitObserver {
// -------------------------- OTHER METHODS --------------------------

    void onAppExit();
}