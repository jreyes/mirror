package com.vaporwarecorp.mirror.feature.watch;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.component.command.HoundifyCommand;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter;

import static com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter.VIDEO_URL;

@Plugin
public class WatchCBSCommand extends AbstractHoundifyCommand implements HoundifyCommand {
// ------------------------------ FIELDS ------------------------------

    private static final String COMMAND_EXPRESSION = "((\"watch\"|\"display\").(\"cbs\"|\"c b s\"|\"c. b. s.\"))";
    private static final String COMMAND_INTENT = "WatchCBS";
    private static final String COMMAND_RESPONSE = "Ok, displaying CBS";
    private static final String COMMAND_URL =
            "http://cbsnewshd-lh.akamaihd.net/i/CBSNHD_7@199302/index_700_av-p.m3u8?sd=10&rebase=on";

    @Plug
    EventManager mEventManager;
    @Plug
    MainFeature mFeature;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public WatchCBSCommand() {
        super(COMMAND_INTENT, COMMAND_EXPRESSION, COMMAND_RESPONSE);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        mEventManager.post(new SpeechEvent(result.getSpokenResponseLong()));
        mFeature.showPresenter(VideoPlayerPresenter.class, new Params(VIDEO_URL, COMMAND_URL));
    }

    @Override
    public String getCommandTypeValue() {
        return COMMAND_INTENT;
    }
}
