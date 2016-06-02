/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.feature.spotify;

import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.util.Params;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.feature.Command;
import com.vaporwarecorp.mirror.feature.MainFeature;

import java.util.List;

import static com.vaporwarecorp.mirror.feature.spotify.SpotifyPresenter.TRACK_IDS;

@Plugin
public class SpotifySearchCommand extends AbstractSpotifyCommand implements Command {
// ------------------------------ FIELDS ------------------------------

    @Plug
    MainFeature mFeature;

// --------------------------- CONSTRUCTORS ---------------------------

    @Scope(MirrorAppScope.class)
    @Provides(Command.class)
    public SpotifySearchCommand() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public String getCommandTypeValue() {
        return "MusicSearchCommand";
    }

    @Override
    protected void onExecuteCommandSuccess(CommandResult result, List<String> trackIds) {
        Params params = new Params();
        params.put(TRACK_IDS, trackIds);
        mFeature.showPresenter(SpotifyPresenter.class, params);

        String spokenResponseLong = textValue(
                result.getJsonNode().findValue("AutoPlayPreviewResult"), "SpokenResponseLong");
        mFeature.speak(spokenResponseLong);
    }
}
