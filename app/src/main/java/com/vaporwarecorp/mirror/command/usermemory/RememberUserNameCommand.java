package com.vaporwarecorp.mirror.command.usermemory;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;
import timber.log.Timber;

public class RememberUserNameCommand extends AbstractUserMemoryCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        Timber.d("RememberUserNameCommand " + getUserName(result));
        MirrorApp.preference(activity).setUserName(getUserName(result));
        super.executeCommand(result, activity);
    }

    @Override
    public String getCommandTypeValue() {
        return "RememberUserName";
    }
}
