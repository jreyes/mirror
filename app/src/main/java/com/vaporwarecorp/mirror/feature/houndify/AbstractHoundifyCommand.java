/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.feature.houndify;

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
