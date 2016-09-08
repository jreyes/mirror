package com.vaporwarecorp.mirror.feature.pocketsphinx;

public interface PocketSphinxCommand {
// -------------------------- OTHER METHODS --------------------------

    void executeCommand(String command);

    boolean matches(String command);
}
