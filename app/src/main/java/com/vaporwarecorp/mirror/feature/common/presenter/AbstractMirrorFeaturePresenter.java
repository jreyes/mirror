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

import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.util.RxUtil;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public abstract class AbstractMirrorFeaturePresenter<T extends View> extends AbstractFeaturePresenter<T> {
// ------------------------------ FIELDS ------------------------------

    private CompositeSubscription mCompositeSubscription;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        super.onPlugged(bus);
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void onUnplugged(PluginBus bus) {
        if (mCompositeSubscription != null && !mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription.unsubscribe();
            mCompositeSubscription = null;
        }
        super.onUnplugged(bus);
    }

    protected void debounce(Action1<? super Long> action, long delay) {
        mCompositeSubscription.add(RxUtil.debounce(action, delay));
    }
}
