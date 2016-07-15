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
package com.vaporwarecorp.mirror.feature.main;

import android.content.Intent;
import com.robopupu.api.feature.FeaturePresenter;
import com.robopupu.api.mvp.Presenter;
import com.robopupu.api.plugin.PlugInterface;

@PlugInterface
public interface MainPresenter extends FeaturePresenter {
// -------------------------- OTHER METHODS --------------------------

    void onViewResult(int requestCode, int resultCode, Intent data);

    void processCommand(int resultCode, Intent data);

    void removeView(Class<? extends Presenter> presenterClass);

    void speak(String textToSpeak);

    void startListening();

    void stopListening();

    void test1();

    void test2();

    void test3();

    void test4();

    void test5();

    void test6();

    void verifyPermissions();
}