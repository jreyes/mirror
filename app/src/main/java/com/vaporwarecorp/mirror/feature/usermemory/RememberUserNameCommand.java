package com.vaporwarecorp.mirror.feature.usermemory;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PreferenceManager;
import com.vaporwarecorp.mirror.feature.Command;

@Plugin
public class RememberUserNameCommand extends AbstractUserMemoryCommand implements Command {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    PreferenceManager mPreferenceManager;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public RememberUserNameCommand() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        onSuccess(mEventManager, result.getSpokenResponseLong());
        mPreferenceManager.setUserName(getUserName(result));
    }

    @Override
    public String getCommandTypeValue() {
        return "RememberUserName";
    }
}
