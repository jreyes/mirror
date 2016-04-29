package com.vaporwarecorp.mirror.event;

public class SpeechEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    private final String message;

// --------------------------- CONSTRUCTORS ---------------------------

    public SpeechEvent(String message) {
        this.message = message;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getMessage() {
        return message;
    }
}
