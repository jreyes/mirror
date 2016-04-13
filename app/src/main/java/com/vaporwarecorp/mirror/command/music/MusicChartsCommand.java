package com.vaporwarecorp.mirror.command.music;

import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

public class MusicChartsCommand extends AbstractMusicCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        activity.speak(result.getSpokenResponseLong());
        super.executeCommand(result, activity);
    }

    @Override
    public String getCommandTypeValue() {
        return "MusicChartsCommand";
    }
}
