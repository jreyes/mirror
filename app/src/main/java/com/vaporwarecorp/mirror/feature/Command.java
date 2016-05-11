package com.vaporwarecorp.mirror.feature;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.plugin.PluginComponent;

public interface Command extends PluginComponent {
// -------------------------- OTHER METHODS --------------------------

    void executeCommand(CommandResult result);

    String getCommandKind();

    String getCommandTypeKey();

    String getCommandTypeValue();

    boolean matches(CommandResult commandResult);
}
