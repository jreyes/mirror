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
package com.vaporwarecorp.mirror.feature.configuration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureDialogFragment;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.util.FullScreenUtil;
import com.vaporwarecorp.mirror.util.NetworkUtil;

@Plugin
@Provides(ConfigurationView.class)
public class ConfigurationFragment extends FeatureDialogFragment<ConfigurationPresenter> implements ConfigurationView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    ConfigurationPresenter mPresenter;

    private TextView mIpAddressView;

// --------------------------- CONSTRUCTORS ---------------------------

    public ConfigurationFragment() {
        setStyle(STYLE_NO_FRAME, R.style.ConfigurationView);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface ConfigurationView ---------------------

    @Override
    public void setIpAddress(String ipAddress) {
        mIpAddressView.setText(ipAddress);
    }

// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onCenterDisplay() {
    }

    @Override
    public void onSideDisplay() {
    }

    @Override
    public Class presenterClass() {
        return ConfigurationPresenter.class;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public ConfigurationPresenter getPresenter() {
        return mPresenter;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        return inflater.inflate(R.layout.fragment_configuration, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        FullScreenUtil.onResume(getDialog());
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();
        mIpAddressView = getView(R.id.ip_address_view);
        getView(R.id.dismiss_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.dismiss();
            }
        });

        final String ipAddress = NetworkUtil.ipAddress(getActivity());
        if (ipAddress != null) {
            mIpAddressView.setText(getString(R.string.configuration_ip_address, ipAddress));
        } else {
            mIpAddressView.setText(getString(R.string.configuration_no_ip_address));
        }
    }
}
