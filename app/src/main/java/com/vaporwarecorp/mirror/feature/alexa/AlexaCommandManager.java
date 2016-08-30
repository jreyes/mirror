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
package com.vaporwarecorp.mirror.feature.alexa;

import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.component.CommandManager;
import com.vaporwarecorp.mirror.component.configuration.Configuration;

@PlugInterface
public interface AlexaCommandManager extends CommandManager, Configuration {
// ------------------------------ FIELDS ------------------------------

    String PREF = AlexaCommandManager.class.getName();
    String PREF_COGNITO_POOL_ID = PREF + ".PREF_COGNITO_POOL_ID";
    String PREF_IOT_ENDPOINT = PREF + ".PREF_IOT_ENDPOINT";
    String PREF_IOT_TOPIC = PREF + ".PREF_IOT_TOPIC";
}
