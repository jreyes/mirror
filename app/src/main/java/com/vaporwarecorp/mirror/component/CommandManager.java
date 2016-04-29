package com.vaporwarecorp.mirror.component;

import android.content.Intent;
import com.robopupu.api.component.Manager;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface CommandManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void processCommand(int resultCode, Intent data);

    void voiceSearch();
}
