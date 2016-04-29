package com.vaporwarecorp.mirror.event;

public class CommandEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    public static final String TYPE_COMMAND_SUCCESS = "TYPE_COMMAND_SUCCESS";
    public static final String TYPE_COMMAND_ERROR = "TYPE_COMMAND_ERROR";

    private final String message;
    private final String type;

// --------------------------- CONSTRUCTORS ---------------------------

    public CommandEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
