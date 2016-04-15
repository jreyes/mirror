package com.vaporwarecorp.mirror.command.music;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

import java.util.ArrayList;

public class MusicChartsCommand extends AbstractMusicCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public String getCommandTypeValue() {
        return "MusicChartsCommand";
    }

    @Override
    protected void onExecuteCommandSuccess(CommandResult result, MirrorActivity activity, ArrayList<String> trackIds) {
        activity.speak(result.getSpokenResponseLong());
        super.onExecuteCommandSuccess(result, activity, trackIds);
    }
}
