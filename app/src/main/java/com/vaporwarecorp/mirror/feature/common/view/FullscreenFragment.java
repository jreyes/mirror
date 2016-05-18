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

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;

public abstract class FullscreenFragment<T extends FeaturePresenter> extends FeatureFragment<T> implements FullscreenView {
// ------------------------------ FIELDS ------------------------------

    private ImageView mImageView;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FullscreenView ---------------------

    @Override
    public boolean isLandscape() {
        final int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        return rotation == ROTATION_0 || rotation == ROTATION_180;
    }

    @Override
    public void setPictureUrl(String pictureUrl) {
        Glide.with(this)
                .load(pictureUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
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
