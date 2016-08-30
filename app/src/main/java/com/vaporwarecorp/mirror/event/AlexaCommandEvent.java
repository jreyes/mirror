package com.vaporwarecorp.mirror.event;

public class AlexaCommandEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    private final String command;

// --------------------------- CONSTRUCTORS ---------------------------

    public AlexaCommandEvent(String command) {
        this.command = command;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getCommand() {
        return command;
    }
}
