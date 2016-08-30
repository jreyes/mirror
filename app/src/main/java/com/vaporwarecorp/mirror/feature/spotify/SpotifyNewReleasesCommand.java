package com.vaporwarecorp.mirror.feature.spotify;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.AbstractPluginComponent;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.SpeechEvent;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.alexa.AlexaCommand;

import static com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter.TRACK_IDS;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(Command.class)
public class SpotifyNewReleasesCommand extends AbstractPluginComponent implements AlexaCommand {
// ------------------------------ FIELDS ------------------------------

    private static final String COMMAND_EXPRESSION = "new releases from spot";

    @Plug
    EventManager mEventManager;
    @Plug
    MainFeature mFeature;
    @Plug
    SpotifyManager mSpotifyManager;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AlexaCommand ---------------------

    @Override
    public void executeCommand(String command) {
        mSpotifyManager.getNewReleases(trackUris -> {
            mEventManager.post(new SpeechEvent(""));

            Params params = new Params();
            params.put(TRACK_IDS, trackUris);
            mFeature.showPresenter(SpotifyPresenter.class, params);
        });
    }

    @Override
    public boolean matches(String command) {
        return COMMAND_EXPRESSION.equals(command);
    }
}
