package com.vaporwarecorp.mirror.command.map;

import android.app.Fragment;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;

public class ShowDirectionsCommand extends AbstractMapCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public String getCommandTypeValue() {
        return "ShowDirections";
    }

    @Override
    protected Fragment getMapFragment(CommandResult result) {
        JsonNode data = result.getNativeData();

        JsonNode from = data.findValue("StartMapLocationSpec");
        String fromTitle = textValue(from, "Label");
        double fromLatitude = doubleValue(from, "Latitude");
        double fromLongitude = doubleValue(from, "Longitude");

        JsonNode to = data.findValue("DestinationMapLocationSpec");
        String toTitle = textValue(from, "Label");
        double toLatitude = doubleValue(to, "Latitude");
        double toLongitude = doubleValue(to, "Longitude");

        return MapFragment.newInstance(fromTitle, fromLatitude, fromLongitude, toTitle, toLatitude, toLongitude);
    }
}
