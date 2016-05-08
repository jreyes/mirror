package com.vaporwarecorp.mirror.component.command;

import com.hound.core.model.sdk.ClientMatch;
import com.vaporwarecorp.mirror.feature.Command;

public interface HoundifyCommand extends Command {
// -------------------------- OTHER METHODS --------------------------

    ClientMatch getClientMatch();
}
