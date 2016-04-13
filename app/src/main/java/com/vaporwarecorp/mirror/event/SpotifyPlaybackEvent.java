package com.vaporwarecorp.mirror.event;

import com.spotify.sdk.android.player.PlayerNotificationCallback.EventType;

public class SpotifyPlaybackEvent {
// ------------------------------ FIELDS ------------------------------

    private EventType eventType;
    private int lastPosition;

// --------------------------- CONSTRUCTORS ---------------------------

    public SpotifyPlaybackEvent(int lastPosition, EventType eventType) {
        this.lastPosition = lastPosition;
        this.eventType = eventType;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public EventType getEventType() {
        return eventType;
    }

    public int getLastPosition() {
        return lastPosition;
    }
}
