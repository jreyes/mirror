package com.vaporwarecorp.mirror.command.nomatch;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

public class NoMatchCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        activity.speak(result.getSpokenResponseLong());
        activity.startListening();
    }

    @Override
    public String getCommandTypeValue() {
        return null;
    }

    @Override
    public boolean matches(CommandResult commandResult) {
        return true;
    }

    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
    }
}
