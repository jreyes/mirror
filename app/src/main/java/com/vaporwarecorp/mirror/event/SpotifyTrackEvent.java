package com.vaporwarecorp.mirror.event;

import kaaes.spotify.webapi.android.models.Track;

public class SpotifyTrackEvent {
// ------------------------------ FIELDS ------------------------------

    private Track track;

// --------------------------- CONSTRUCTORS ---------------------------

    public SpotifyTrackEvent(Track track) {
        this.track = track;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public Track getTrack() {
        return track;
    }
}
