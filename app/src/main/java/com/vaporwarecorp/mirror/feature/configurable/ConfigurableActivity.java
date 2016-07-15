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
package com.vaporwarecorp.mirror.feature.configurable;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.mvp.PluginActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.app.MirrorApplication;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.util.FullScreenUtil;
import com.vaporwarecorp.mirror.util.NetworkUtil;

@Plugin
@Provides(ConfigurableView.class)
public class ConfigurableActivity extends PluginActivity<ConfigurablePresenter> implements ConfigurableView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug(MirrorAppScope.class)
    ConfigurableFeature mFeature;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug(ConfigurableScope.class)
    ConfigurablePresenter mPresenter;

    private TextView mIpAddressView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurableView ---------------------

    @Override
    public void setIpAddress(String ipAddress) {
        mIpAddressView.setText(ipAddress);
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public ConfigurablePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onCreate(final Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_configurable);
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();
        mIpAddressView = getView(R.id.ip_address_view);
        getView(R.id.dismiss_button).setOnClickListener(v -> {
            mPresenter.dismiss();
        });

        final String ipAddress = NetworkUtil.ipAddress(this);
        if (ipAddress != null) {
            mIpAddressView.setText(getString(R.string.configuration_ip_address, ipAddress));
        } else {
            mIpAddressView.setText(getString(R.string.configuration_no_ip_address));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MirrorApplication.refWatcher(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        FullScreenUtil.onResume(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mFeature.isStarted()) {
            mFeatureManager.startFeature(this, mFeature);
        }
    }
}
