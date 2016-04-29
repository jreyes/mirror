package com.vaporwarecorp.mirror.feature;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface Command {
// -------------------------- OTHER METHODS --------------------------

    void executeCommand(CommandResult result);

    String getCommandKind();

    String getCommandTypeKey();

    String getCommandTypeValue();

    boolean matches(CommandResult commandResult);
}
