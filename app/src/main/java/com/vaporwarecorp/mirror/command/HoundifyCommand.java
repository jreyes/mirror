package com.vaporwarecorp.mirror.command;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

public interface HoundifyCommand {
// -------------------------- OTHER METHODS --------------------------

    void executeCommand(CommandResult result, MirrorActivity activity);

    String getCommandKind();

    String getCommandTypeKey();

    String getCommandTypeValue();

    boolean matches(CommandResult commandResult);

    void registerHoundify(HoundifyManager houndifyManager);
}
