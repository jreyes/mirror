package com.vaporwarecorp.mirror.feature.houndify;

import android.content.Intent;
import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.component.CommandManager;
import com.vaporwarecorp.mirror.component.configuration.Configuration;

@PlugInterface
public interface HoundifyCommandManager extends CommandManager, Configuration {
// -------------------------- OTHER METHODS --------------------------

    void processCommand(int resultCode, Intent data);
}
