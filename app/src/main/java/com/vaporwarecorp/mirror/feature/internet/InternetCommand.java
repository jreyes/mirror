package com.vaporwarecorp.mirror.feature.internet;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.component.command.HoundifyCommand;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter;

import static com.vaporwarecorp.mirror.feature.common.presenter.VideoPlayerPresenter.VIDEO_URL;

@Plugin
public class InternetCommand extends AbstractHoundifyCommand implements HoundifyCommand {
// ------------------------------ FIELDS ------------------------------

    private static final String COMMAND_EXPRESSION = "connect to the internet";
    private static final String COMMAND_INTENT = "ConnectToTheInternet";
    private static final String COMMAND_RESPONSE = "Ok, connecting to the internet";

    @Plug
    AppManager mAppManager;
    @Plug
    MainFeature mFeature;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public InternetCommand() {
        super(COMMAND_INTENT, COMMAND_EXPRESSION, COMMAND_RESPONSE);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        final String videoUrl = mAppManager.getLocalAssetPath("videos/aol.mp4");
        mFeature.showPresenter(VideoPlayerPresenter.class, new Params(VIDEO_URL, videoUrl));
    }

    @Override
    public String getCommandTypeValue() {
        return COMMAND_INTENT;
    }
}
