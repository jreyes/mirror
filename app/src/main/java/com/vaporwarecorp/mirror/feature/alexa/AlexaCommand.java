package com.vaporwarecorp.mirror.feature.alexa;

import com.vaporwarecorp.mirror.feature.Command;

public interface AlexaCommand extends Command {
// -------------------------- OTHER METHODS --------------------------

    void executeCommand(String command);

    boolean matches(String command);
}
