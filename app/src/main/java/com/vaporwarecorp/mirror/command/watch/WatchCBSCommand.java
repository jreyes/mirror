package com.vaporwarecorp.mirror.command.watch;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;
import com.vaporwarecorp.mirror.ui.fragment.VideoPlayerFragment;

public class WatchCBSCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        String cbs = "http://cbsnewshd-lh.akamaihd.net/i/CBSNHD_7@199302/index_700_av-p.m3u8?sd=10&rebase=on";
        activity.displayFragment(VideoPlayerFragment.newInstance(cbs));
        activity.startHandGestures();
    }

    @Override
    public String getCommandTypeValue() {
        return "WATCH_CBS";
    }

    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
        houndifyManager.registerClientMatch(
                getCommandTypeValue(),
                "((\"watch\"|\"display\").(\"cbs\"|\"c b s\"|\"c. b. s.\"))",
                "Ok, displaying CBS"
        );
    }
}
