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

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.feature.AbstractCommand;
import timber.log.Timber;

import java.util.LinkedList;
import java.util.List;

abstract class AbstractSpotifyCommand extends AbstractCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public void executeCommand(CommandResult result) {
        Timber.d("executeCommand " + getClass().getName());
        List<String> trackIds = getTrackIds(result);
        if (trackIds.isEmpty()) {
            //activity.onError();
        } else {
            //mFeature.showPresenter(SpotifyPresenter.class);
            onExecuteCommandSuccess(result, trackIds);
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

    protected abstract void onExecuteCommandSuccess(CommandResult result, List<String> trackIds);

    @SuppressWarnings("Convert2streamapi")
    private List<String> getTrackIds(CommandResult result) {
        List<String> trackIds = new LinkedList<>();
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
