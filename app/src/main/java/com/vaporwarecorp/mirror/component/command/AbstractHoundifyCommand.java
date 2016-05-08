package com.vaporwarecorp.mirror.component.command;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hound.core.model.sdk.ClientMatch;
import com.vaporwarecorp.mirror.feature.AbstractCommand;

public abstract class AbstractHoundifyCommand extends AbstractCommand implements HoundifyCommand {
// ------------------------------ FIELDS ------------------------------

    private final ClientMatch mClientMatch;
    private final JsonNodeFactory mNodeFactory;

// --------------------------- CONSTRUCTORS ---------------------------

    public AbstractHoundifyCommand(String intent, String expression, String response) {
        mNodeFactory = JsonNodeFactory.instance;

        mClientMatch = new ClientMatch();
        mClientMatch.setExpression(expression);
        mClientMatch.setAllResponses(response);
        mClientMatch.setResult(mNodeFactory.objectNode().put("Intent", intent));
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

    public ClientMatch getClientMatch() {
        return mClientMatch;
    }

    protected JsonNodeFactory nodeFactory() {
        return mNodeFactory;
    }
}
