package com.vaporwarecorp.mirror.event;

public class ApplicationEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    public static final String READY = "READY";

    private final String type;

// --------------------------- CONSTRUCTORS ---------------------------

    public ApplicationEvent(String type) {
        this.type = type;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getType() {
        return type;
    }
}
