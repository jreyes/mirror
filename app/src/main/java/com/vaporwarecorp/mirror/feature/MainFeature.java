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
package com.vaporwarecorp.mirror.feature;

import com.robopupu.api.feature.Feature;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.PlugInterface;
import com.robopupu.api.util.Params;

@PlugInterface
public interface MainFeature extends Feature {
// -------------------------- OTHER METHODS --------------------------

    void displayView();

    void hideCurrentPresenter();

    void hidePresenter(Class<? extends Presenter> presenterClass);

    void hideView();

    void onApplicationReady();

    void showPresenter(Class<? extends Presenter> presenterClass, Params... params);
}
