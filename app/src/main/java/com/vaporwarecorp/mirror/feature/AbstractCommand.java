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
package com.vaporwarecorp.mirror.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.robopupu.api.plugin.AbstractPluginComponent;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.CommandEvent;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractCommand extends AbstractPluginComponent implements Command {
// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractCommand) {
            AbstractCommand command = (AbstractCommand) o;
            return getCommandKind().equals(command.getCommandKind()) &&
                    getCommandTypeKey().equals(command.getCommandTypeKey()) &&
                    getCommandTypeValue().equals(command.getCommandTypeValue());
        }
        return false;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Command ---------------------

    @Override
    public String getCommandKind() {
        return "ClientMatchCommand";
    }

    @Override
    public String getCommandTypeKey() {
        return "Intent";
    }

    @Override
    public boolean matches(CommandResult commandResult) {
        if (!getCommandKind().equals(commandResult.getCommandKind())) {
            return false;
        }

        JsonNode key = commandResult.getJsonNode().get(getCommandTypeKey());
        if (key != null) {
            return getCommandTypeValue().equals(key.textValue());
        }
        key = commandResult.getNativeData();
        if (key != null) {
            key = key.findValue(getCommandTypeKey());
            if (key != null) {
                return getCommandTypeValue().equals(key.textValue());
            }
        }
        return false;
    }

    protected Double doubleValue(JsonNode node, String key) {
        JsonNode keyNode = jsonNodeValue(node, key);
        if (keyNode != null) {
            return keyNode.doubleValue();
        }
        return null;
    }

    protected void onError(EventManager eventManager, String message) {
        eventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_ERROR, message));
    }

    protected void onSuccess(EventManager eventManager, String message) {
        eventManager.post(new CommandEvent(CommandEvent.TYPE_COMMAND_SUCCESS, message));
    }

    protected String text(JsonNode node, String key) {
        JsonNode keyNode = jsonNodeValue(node, key);
        if (keyNode != null) {
            keyNode = keyNode.get(0);
            if (keyNode != null) {
                return StringUtils.trimToNull(keyNode.textValue());
            }
        }
        return null;
    }

    protected String textValue(JsonNode node, String key) {
        JsonNode keyNode = jsonNodeValue(node, key);
        if (keyNode != null) {
            return StringUtils.trimToNull(keyNode.textValue());
        }
        return null;
    }

    private JsonNode jsonNodeValue(JsonNode node, String key) {
        if (node != null) {
            JsonNode keyNode = node.findValue(key);
            if (keyNode != null) {
                return keyNode;
            }
        }
        return null;
    }
}
