package com.vaporwarecorp.mirror.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;
import timber.log.Timber;

public class PictureFullscreenFragment extends Fragment {
// ------------------------------ FIELDS ------------------------------

    private static final String PICTURE_URL = "PICTURE_URL";

// -------------------------- STATIC METHODS --------------------------

    public static Fragment newInstance(String pictureUrl) {
        Bundle args = new Bundle();
        args.putString(PICTURE_URL, pictureUrl);

        Fragment fragment = new PictureFullscreenFragment();
        fragment.setArguments(args);
        return fragment;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_fullscreen, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MirrorApp.refWatcher(getActivity()).watch(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String pictureUrl = getArguments().getString(PICTURE_URL);
        Timber.d("Loading %s", pictureUrl);
        Glide.with(this)
                .load(pictureUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                //.placeholder(R.drawable.loading_spinner)
                .into((ImageView) view.findViewById(R.id.picture_view));
    }
}
