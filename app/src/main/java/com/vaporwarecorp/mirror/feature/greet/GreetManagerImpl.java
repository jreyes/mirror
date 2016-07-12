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
package com.vaporwarecorp.mirror.feature.greet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.component.PreferenceManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.vaporwarecorp.mirror.util.JsonUtil.*;
import static com.vaporwarecorp.mirror.util.RandomUtil.randomString;

@Plugin
@Provides(GreetManager.class)
public class GreetManagerImpl extends AbstractMirrorManager implements GreetManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = GreetPresenter.class.getName();
    private static final String PREF_GOODBYE = PREF + ".PREF_GOODBYE";
    private static final List<String> PREF_GOODBYE_DEFAULT = Collections.singletonList(
            "<b>Goodbye</b><br>{{user}}"
    );
    private static final String PREF_WELCOME = PREF + ".PREF_WELCOME";
    private static final List<String> PREF_WELCOME_DEFAULT = Collections.singletonList(
            "<b>Welcome</b><br>{{user}}"
    );
    private static final String TEMPLATE_USER = "{{user}}";

    @Plug
    ConfigurationManager mConfigurationManager;
    @Plug
    PreferenceManager mPreferenceManager;

    private ArrayList<String> mGoodbyes;
    private ArrayList<String> mWelcomes;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/greet.json";
    }

    @Override
    public String getJsonValues() {
        final ArrayNode formItems = createArrayNode();
        for (int i = 0; i < mWelcomes.size(); i++) {
            formItems.add(createTextNode("welcome", mWelcomes.get(i)).put("goodbye", mGoodbyes.get(i)));
        }
        return createJsonNode("formItems", formItems).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateStringSet(PREF_WELCOME, jsonNode, "welcome");
        mConfigurationManager.updateStringSet(PREF_GOODBYE, jsonNode, "goodbye");
        loadConfiguration();
    }

// --------------------- Interface GreetManager ---------------------

    @Override
    public String goodbye() {
        return randomString(mGoodbyes, TEMPLATE_USER, mPreferenceManager.getUserName());
    }

    @Override
    public String welcome() {
        return randomString(mWelcomes, TEMPLATE_USER, mPreferenceManager.getUserName());
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeatureStart() {
        if (mWelcomes.isEmpty() || mGoodbyes.isEmpty()) {
            onFeatureStop();
            return;
        }
        mConfigurationManager.enablePresenter(GreetPresenter.class);
    }

    @Override
    public void onFeatureStop() {
        mConfigurationManager.disablePresenter(GreetPresenter.class);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    private void loadConfiguration() {
        mWelcomes = mConfigurationManager.getStringList(PREF_WELCOME, PREF_WELCOME_DEFAULT);
        mGoodbyes = mConfigurationManager.getStringList(PREF_GOODBYE, PREF_GOODBYE_DEFAULT);
    }
}
