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
package com.vaporwarecorp.mirror.feature.splash;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.ConfigurationManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import timber.log.Timber;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.vaporwarecorp.mirror.util.JsonUtil.*;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SplashManager.class)
public class SplashManagerImpl extends AbstractMirrorManager implements SplashManager {
// ------------------------------ FIELDS ------------------------------

    private static final String PREF = SplashManager.class.getName();
    private static final String PREF_URLS = PREF + ".PREF_URLS";
    private static final List<String> PREF_URLS_DEFAULT = Arrays.asList(
            "http://i.imgur.com/BraqUmV.jpg",
            "http://i.imgur.com/z0hP3uU.jpg",
            "http://i.imgur.com/Vo4uUFC.jpg",
            "http://i.imgur.com/MCQQWYY.jpg",
            "http://i.imgur.com/JmwDKRT.jpg",
            "http://i.imgur.com/Y0Vvy0Y.jpg"
    );

    @Plug
    ConfigurationManager mConfigurationManager;

    private List<String> mUrls;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configuration ---------------------

    @Override
    public String getJsonConfiguration() {
        return "configuration/json/splash.json";
    }

    @Override
    public String getJsonValues() {
        final ArrayNode formItems = createArrayNode();
        for (String url : mUrls) {
            formItems.add(createTextNode("url", url));
        }
        return createJsonNode("formItems", formItems).toString();
    }

    @Override
    public void updateConfiguration(JsonNode jsonNode) {
        mConfigurationManager.updateStringSet(PREF_URLS, jsonNode, "url");
        loadConfiguration();
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        loadConfiguration();
    }

    @Override
    public String getRandomSplash() {
        final String pictureUrl = mUrls.get(new Random().nextInt(mUrls.size()));
        Timber.d("Displaying %s", pictureUrl);
        return pictureUrl;
    }

    private void loadConfiguration() {
        mUrls = mConfigurationManager.getStringList(PREF_URLS, PREF_URLS_DEFAULT);
    }
}
