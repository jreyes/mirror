/*
 *   Copyright 2016 Johann Reyes
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vaporwarecorp.mirror.event;

public class ArtikEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    public static final String TYPE_ACTION_DOWN = "TYPE_ACTION_DOWN";
    public static final String TYPE_ACTION_HOME = "TYPE_ACTION_HOME";
    public static final String TYPE_ACTION_LEFT = "TYPE_ACTION_LEFT";
    public static final String TYPE_ACTION_RIGHT = "TYPE_ACTION_RIGHT";
    public static final String TYPE_ACTION_UP = "TYPE_ACTION_UP";

    private final String type;

    @Override
    public String toString() {
        return "ArtikEvent{" +
                "type='" + type + '\'' +
                '}';
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public ArtikEvent(String type) {
        this.type = type;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getType() {
        return type;
    }
}
