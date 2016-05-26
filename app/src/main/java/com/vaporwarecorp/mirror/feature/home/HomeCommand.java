package com.vaporwarecorp.mirror.feature.home;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.component.command.HoundifyCommand;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;

@Plugin
public class HomeCommand extends AbstractHoundifyCommand implements HoundifyCommand {
// ------------------------------ FIELDS ------------------------------

    private static final String COMMAND_EXPRESSION = "home";
    private static final String COMMAND_INTENT = "Home";
    private static final String COMMAND_RESPONSE = "Ok";

    @Plug
    EventManager mEventManager;
    @Plug
    MainFeature mFeature;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public HomeCommand() {
        super(COMMAND_INTENT, COMMAND_EXPRESSION, COMMAND_RESPONSE);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        mFeature.hideCurrentPresenter();
        mEventManager.post(new SpeechEvent("Ok"));
    }

    @Override
    public String getCommandTypeValue() {
        return COMMAND_INTENT;
    }
}
