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
package com.vaporwarecorp.mirror.event;

public class HotWordEvent implements Event {
// ------------------------------ FIELDS ------------------------------

    public static int TYPE_ALEXA = 2;
    public static int TYPE_GOOGLE = 1;
    public static int TYPE_HOUNDIFY = 3;
    public static int TYPE_NONE = 0;

    private final int type;

// --------------------------- CONSTRUCTORS ---------------------------

    public HotWordEvent(int type) {
        this.type = type;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getType() {
        return type;
    }
}
