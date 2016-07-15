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
package com.vaporwarecorp.mirror.feature.twilio;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.twilio.conversations.IncomingInvite;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.event.ResetEvent;

@Plugin
@Provides(TwilioPresenter.class)
public class TwilioPresenterImpl extends AbstractFeaturePresenter<TwilioView> implements TwilioPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    EventManager mEventManager;
    @Plug
    TwilioManager mTwilioManager;
    @Plug
    TwilioView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(TwilioView.class);
    }

// --------------------- Interface TwilioPresenter ---------------------

    @Override
    public void endCall() {
        mEventManager.post(new ResetEvent(TwilioPresenter.class));
    }

    @Override
    public void rejectCall(IncomingInvite incomingInvite) {
        incomingInvite.reject();
        endCall();
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(View view) {
        super.onViewResume(view);
        mView.displayInvite(mTwilioManager.getCurrentIncomingInvite());
    }

    @Override
    protected TwilioView getViewPlug() {
        return mView;
    }
}
