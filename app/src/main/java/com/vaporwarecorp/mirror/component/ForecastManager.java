package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface ForecastManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void retrieveForecast();

    void startReceiver();
}
