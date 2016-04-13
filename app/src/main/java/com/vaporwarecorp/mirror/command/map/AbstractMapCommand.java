package com.vaporwarecorp.mirror.command.map;

import android.app.Fragment;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.command.AbstractHoundifyCommand;
import com.vaporwarecorp.mirror.manager.HoundifyManager;
import com.vaporwarecorp.mirror.ui.activity.MirrorActivity;

public abstract class AbstractMapCommand extends AbstractHoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public void executeCommand(CommandResult result, MirrorActivity activity) {
        activity.displayFragment(getMapFragment(result));
        activity.speak(result.getSpokenResponseLong());
        activity.startListening();
    }

    @Override
    public String getCommandKind() {
        return "MapCommand";
    }

    @Override
    public String getCommandTypeKey() {
        return "MapCommandKind";
    }

    @Override
    public void registerHoundify(HoundifyManager houndifyManager) {
        houndifyManager.registerCommand(this);
    }

    protected Fragment getMapFragment(CommandResult result) {
        JsonNode data = result.getNativeData();
        String title = data.findValue("Label").textValue();
        double latitude = data.findValue("Latitude").doubleValue();
        double longitude = data.findValue("Longitude").doubleValue();
        return MapFragment.newInstance(title, latitude, longitude);
    }
}
