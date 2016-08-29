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
package com.vaporwarecorp.mirror.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.robopupu.api.component.Manager;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.PlugInterface;

import java.util.ArrayList;
import java.util.List;

@PlugInterface
public interface ConfigurationManager extends Manager {
// -------------------------- OTHER METHODS --------------------------

    void disablePresenter(Class<? extends Presenter> presenterClass);

    void enablePresenter(Class<? extends Presenter> presenterClass);

    boolean getBoolean(String preferenceKey, boolean defaultValue);

    float getFloat(String preferenceKey, float defaultValue);

    int getInt(String preferenceKey, int defaultValue);

    String getString(String preferenceKey, String defaultValue);

    ArrayList<String> getStringList(String preferenceKey, List<String> defaultValue);

    boolean isPresenterEnabled(Class<? extends Presenter> presenterClass);

    void start();

    void stop();

    void updateBoolean(String preferenceKey, JsonNode jsonNode, String jsonNodeKey);

    void updateFloat(String preferenceKey, JsonNode jsonNode, String jsonNodeKey);

    void updateInt(String preferenceKey, JsonNode jsonNode, String jsonNodeKey);

    void updateString(String preferenceKey, String preferenceValue);

    void updateString(String preferenceKey, JsonNode jsonNode, String jsonNodeKey);

    void updateStringSet(String preferenceKey, JsonNode jsonNode, String jsonNodeKey);
}
