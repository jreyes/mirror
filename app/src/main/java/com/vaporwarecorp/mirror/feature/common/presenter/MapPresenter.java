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
package com.vaporwarecorp.mirror.feature.common.presenter;

import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.plugin.PlugInterface;
import com.vaporwarecorp.mirror.component.Shareable;

@PlugInterface
public interface MapPresenter extends FeaturePresenter, Shareable {
// ------------------------------ FIELDS ------------------------------

    String MAP_FROM_LATITUDE = "MAP_FROM_LATITUDE";
    String MAP_FROM_LONGITUDE = "MAP_FROM_LONGITUDE";
    String MAP_FROM_TITLE = "MAP_FROM_TITLE";
    String MAP_TO_LATITUDE = "MAP_TO_LATITUDE";
    String MAP_TO_LONGITUDE = "MAP_TO_LONGITUDE";
    String MAP_TO_TITLE = "MAP_TO_TITLE";
}
