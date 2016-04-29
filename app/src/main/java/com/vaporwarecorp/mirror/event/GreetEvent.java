package com.vaporwarecorp.mirror.event;

public class GreetEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    public static final String TYPE_GOODBYE = "TYPE_GOODBYE";
    public static final String TYPE_WELCOME = "TYPE_WELCOME";

    private final String mType;

// --------------------------- CONSTRUCTORS ---------------------------

    public GreetEvent(String type) {
        mType = type;
    }

// -------------------------- OTHER METHODS --------------------------

    public String getType() {
        return mType;
    }
}
