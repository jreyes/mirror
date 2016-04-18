package com.vaporwarecorp.mirror.command.usermemory;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

public class RememberUserNameCommand extends AbstractUserMemoryCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        MirrorApp.preference(activity).setUserName(getUserName(result));
        super.executeCommand(result, activity);
    }

    @Override
    public String getCommandTypeValue() {
        return "RememberUserName";
    }
}
