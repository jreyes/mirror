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
package com.vaporwarecorp.mirror.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

import java.io.IOException;

public class JsonUtil {
// ------------------------------ FIELDS ------------------------------

    private static final ObjectMapper objectMapper;

// -------------------------- STATIC METHODS --------------------------

    static {
        objectMapper = new ObjectMapper();
        objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        objectMapper.getFactory().enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
    }

    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    public static JsonNode createJsonNode(String key, JsonNode value) {
        return createObjectNode().set(key, value);
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ObjectNode createTextNode(String key, String value) {
        return createObjectNode().put(key, value);
    }

    public static Double doubleValue(JsonNode node, String key) {
        JsonNode keyNode = jsonNodeValue(node, key);
        if (keyNode != null) {
            return keyNode.doubleValue();
        }
        return null;
    }

    public static JsonNode jsonNodeValue(JsonNode node, String key) {
        if (node != null) {
            JsonNode keyNode = node.findValue(key);
            if (keyNode != null) {
                return keyNode;
            }
        }
        return null;
    }

    public static String textValue(JsonNode node, String key) {
        JsonNode keyNode = jsonNodeValue(node, key);
        if (keyNode != null) {
            return StringUtils.trimToNull(keyNode.textValue());
        }
        return null;
    }

    public static JsonNode toJsonNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (IOException e) {
            Timber.e(e, "Error parsing toJsonNode");
            return NullNode.getInstance();
        }
    }

    public static String toString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            Timber.e(e, "Error parsing toString");
            return null;
        }
    }
}
