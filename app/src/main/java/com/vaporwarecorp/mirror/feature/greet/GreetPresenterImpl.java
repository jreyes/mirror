package com.vaporwarecorp.mirror.feature.greet;

import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.AbstractFeaturePresenter;
import com.robopupu.api.mvp.View;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.EventManager;
import com.vaporwarecorp.mirror.component.PreferenceManager;
import com.vaporwarecorp.mirror.event.Event;
import com.vaporwarecorp.mirror.event.GreetEvent;
import com.vaporwarecorp.mirror.util.RxUtil;

import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_GOODBYE;
import static com.vaporwarecorp.mirror.event.GreetEvent.TYPE_WELCOME;

@Plugin
@Provides(GreetPresenter.class)
public class GreetPresenterImpl extends AbstractFeaturePresenter<GreetView> implements GreetPresenter {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    EventManager mEventManager;
    @Plug
    PreferenceManager mPreferenceManager;
    @Plug
    GreetView mView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface GreetPresenter ---------------------

    @Override
    public void onAnimationEnd() {
        RxUtil.delay(l -> mEventManager.post(getGreetEvent()), 5);
    }

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(final PluginBus bus) {
        super.onPlugged(bus);
        plug(GreetView.class);
    }

// --------------------- Interface ViewObserver ---------------------

    @Override
    public void onViewResume(final View view) {
        super.onViewResume(view);
        mView.displayGreet(getGreetName(), isWelcome());
    }

    @Override
    protected GreetView getViewPlug() {
        return mView;
    }

    private Event getGreetEvent() {
        return new GreetEvent(isWelcome() ? TYPE_WELCOME : TYPE_GOODBYE);
    }

    private String getGreetName() {
        return mPreferenceManager.getUserName();
    }

    private boolean isWelcome() {
        return getParams().containsValue(TYPE_WELCOME);
    }
}
