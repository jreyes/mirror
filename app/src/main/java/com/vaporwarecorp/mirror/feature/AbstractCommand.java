package com.vaporwarecorp.mirror.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.CommandEvent;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractCommand implements Command {
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