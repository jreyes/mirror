package com.vaporwarecorp.mirror.command.usermemory;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

abstract class AbstractUserMemoryCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        activity.speak(result.getSpokenResponseLong());
    }

    @Override
    public String getCommandKind() {
        return "UserMemoryCommand";
    }

    @Override
    public String getCommandTypeKey() {
        return "UserMemoryCommandKind";
    }

    @Override
    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
    }

    protected String getUserName(CommandResult result) {
        return textValue(result.getNativeData(), "Name");
    }
}
