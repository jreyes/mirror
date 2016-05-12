package com.vaporwarecorp.mirror.feature.showmap;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.AbstractCommand;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.common.presenter.MapPresenter;

import static com.vaporwarecorp.mirror.feature.common.presenter.MapPresenter.*;

@Plugin
public class ShowMapCommand extends AbstractCommand implements Command {
// ------------------------------ FIELDS ------------------------------

    @Plug
    MainFeature mFeature;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public ShowMapCommand() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        JsonNode data = result.getNativeData();

        final Params params = new Params();
        params.put(MAP_FROM_TITLE, data.findValue("Label").textValue());
        params.put(MAP_FROM_LATITUDE, data.findValue("Latitude").doubleValue());
        params.put(MAP_FROM_LONGITUDE, data.findValue("Longitude").doubleValue());
        mFeature.showPresenter(MapPresenter.class, params);
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
    public String getCommandTypeValue() {
        return "ShowMap";
    }
}
