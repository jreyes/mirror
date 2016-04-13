package com.vaporwarecorp.mirror.command.music;

public class MusicSearchCommand extends AbstractMusicCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    @Override
    public String getCommandTypeValue() {
        return "MusicSearchCommand";
    }
}
