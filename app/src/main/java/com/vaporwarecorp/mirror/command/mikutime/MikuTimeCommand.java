package com.vaporwarecorp.mirror.command.mikutime;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;
import com.vaporwarecorp.mirror.ui.fragment.YouTubeFragment;

import java.util.Random;

public class MikuTimeCommand extends AbstractHoundifyCommand {
// ------------------------------ FIELDS ------------------------------

    private static final String[] MIKU_VIDEO_IDS = {"u99kOUA5EpE", "UygC613BrmE", "TXwW5ZuKlwE", "DHioZY5CdYw"};

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        String videoId = MIKU_VIDEO_IDS[new Random().nextInt(MIKU_VIDEO_IDS.length)];

        activity.hideScreen();
        activity.displayFragment(YouTubeFragment.newInstance(videoId));
        activity.startHandGestures();
    }

    @Override
    public String getCommandTypeValue() {
        return "MIKU_TIME";
    }

    @Override
    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
        houndifyManager.registerClientMatch(
                getCommandTypeValue(),
                "miku time",
                "miku time"
        );
    }
}
