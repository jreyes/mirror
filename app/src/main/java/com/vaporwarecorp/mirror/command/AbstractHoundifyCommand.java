package com.vaporwarecorp.mirror.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.core.model.sdk.CommandResult;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractHoundifyCommand implements HoundifyCommand {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface HoundifyCommand ---------------------

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
