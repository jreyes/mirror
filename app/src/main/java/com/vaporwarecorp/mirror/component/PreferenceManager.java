package com.vaporwarecorp.mirror.component;

import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface PreferenceManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    String getUserName();

    void setUserName(String userName);
}
