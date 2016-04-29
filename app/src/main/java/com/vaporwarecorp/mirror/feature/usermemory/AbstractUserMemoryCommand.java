package com.vaporwarecorp.mirror.feature.usermemory;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.feature.AbstractCommand;

public abstract class AbstractUserMemoryCommand extends AbstractCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public String getCommandKind() {
        return "UserMemoryCommand";
    }

    @Override
    public String getCommandTypeKey() {
        return "UserMemoryCommandKind";
    }

    protected String getUserName(CommandResult result) {
        return textValue(result.getNativeData(), "Name");
    }
}
