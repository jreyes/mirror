package com.vaporwarecorp.mirror.command.music;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

import java.util.ArrayList;

abstract class AbstractMusicCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        ArrayList<String> trackIds = getTrackIds(result);
        if (trackIds.isEmpty()) {
            activity.onError();
        } else {
            onExecuteCommandSuccess(result, activity, trackIds);
        }
    }

    @Override
    public String getCommandKind() {
        return "MusicCommand";
    }

    @Override
    public String getCommandTypeKey() {
        return "MusicCommandKind";
    }

    @Override
    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
    }

    protected void onExecuteCommandSuccess(CommandResult result, MirrorActivity activity, ArrayList<String> trackIds) {
        activity.displayFragment(MusicFragment.newInstance(trackIds));
        activity.startHandGestures();
    }

    @SuppressWarnings("Convert2streamapi")
    private ArrayList<String> getTrackIds(CommandResult result) {
        ArrayList<String> trackIds = new ArrayList<>();
        for (JsonNode thirdPartyIds : result.getNativeData().findValues("MusicThirdPartyIds")) {
            if ("Spotify".equals(textValue(thirdPartyIds, "Name"))) {
                String trackId = text(thirdPartyIds, "Ids");
                if (trackId != null) {
                    trackIds.add(trackId);
                }
            }
        }
        return trackIds;
    }
}
