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
package com.vaporwarecorp.mirror.feature.common;

import com.robopupu.api.plugin.AbstractPluginComponent;
import com.robopupu.api.plugin.PluginBus;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class AbstractMirrorManager extends AbstractPluginComponent implements MirrorManager {
// ------------------------------ FIELDS ------------------------------

    private CompositeSubscription mCompositeSubscription;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
    }

    @Override
    public void onFeatureResume() {
    }

    @Override
    public void onFeatureStart() {
    }

    @Override
    public void onFeatureStop() {
    }

    @Override
    public void onViewStart() {
    }

    @Override
    public void onViewStop() {
    }

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

    protected void subscribe(Subscription subscription) {
        if (mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) {
            return;
        }
        mCompositeSubscription.add(subscription);
    }
}
