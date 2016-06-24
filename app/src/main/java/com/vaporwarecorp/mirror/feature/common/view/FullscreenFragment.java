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
package com.vaporwarecorp.mirror.feature.common.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.feature.FeaturePresenter;
import com.vaporwarecorp.mirror.R;

public abstract class FullscreenFragment<T extends FeaturePresenter>
        extends FeatureFragment<T>
        implements FullscreenView {
// ------------------------------ FIELDS ------------------------------

    private ImageView mImageView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FullscreenView ---------------------

    @Override
    public void setPictureUrl(String pictureUrl) {
        Glide
                .with(this)
                .load(pictureUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
    }

// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public void onCenterDisplay() {
    }

    @Override
    public void onSideDisplay() {
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        return inflater.inflate(R.layout.fragment_picture_fullscreen, container, false);
    }

    @Override
    protected void onCreateBindings() {
        super.onCreateBindings();
        mImageView = getView(R.id.picture_view);
    }
}
