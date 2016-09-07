package com.vaporwarecorp.mirror.app;

public class Constants {
// -------------------------- INNER CLASSES --------------------------

    public interface ACTION {
        String WEB_SERVER_SERVICE_START = "WEB_SERVER_SERVICE_START";
        String WEB_SERVER_SERVICE_STOP = "WEB_SERVER_SERVICE_STOP";
        String ALEXA_COMMAND_SERVICE_START = "ALEXA_COMMAND_SERVICE_START";
        String ALEXA_COMMAND_SERVICE_STOP = "ALEXA_COMMAND_SERVICE_STOP";
    }

    public interface NOTIFICATION_ID {
        int WEB_SERVER_SERVICE = 101;
        int ALEXA_COMMAND_SERVICE = 102;
    }
}
