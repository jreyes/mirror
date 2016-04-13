package com.vaporwarecorp.mirror.command.internet;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;
import com.vaporwarecorp.mirror.ui.fragment.VideoPlayerFragment;

public class InternetCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    public void executeCommand(CommandResult result, MirrorActivity activity) {
        String aolPath = MirrorApp.localAsset(activity).getLocalAssetPath("aol.mp4");
        activity.displayFragment(VideoPlayerFragment.newInstance(aolPath));
    }

    @Override
    public String getCommandTypeValue() {
        return "CONNECT_TO_THE_INTERNET";
    }

    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
        houndifyManager.registerClientMatch(
                getCommandTypeValue(),
                "connect to the internet",
                "Ok, connecting to the internet"
        );
    }
}
