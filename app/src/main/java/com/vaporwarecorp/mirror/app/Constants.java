package com.vaporwarecorp.mirror.app;

public class Constants {
// -------------------------- INNER CLASSES --------------------------

    public interface ACTION {
        String CONFIGURATION_SERVICE_START = "CONFIGURATION_SERVICE_START";
        String CONFIGURATION_SERVICE_STOP = "CONFIGURATION_SERVICE_STOP";
        String ALEXA_COMMAND_SERVICE_START = "ALEXA_COMMAND_SERVICE_START";
        String ALEXA_COMMAND_SERVICE_STOP = "ALEXA_COMMAND_SERVICE_STOP";
    }

    public interface NOTIFICATION_ID {
        int CONFIGURATION_SERVICE = 101;
        int ALEXA_COMMAND_SERVICE = 102;
    }
}
