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

public class ProximityEvent implements Event {

    public static final int SHARE_START = 0;
    public static final int SHARE_STOP = 1;

    private final int type;

// --------------------------- CONSTRUCTORS ---------------------------

    public ProximityEvent(int type) {
        this.type = type;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getType() {
        return type;
    }
}
