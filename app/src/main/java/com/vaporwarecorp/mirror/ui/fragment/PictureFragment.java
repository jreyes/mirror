package com.vaporwarecorp.mirror.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.florent37.glidepalette.GlidePalette;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;

public class PictureFragment extends Fragment {
// ------------------------------ FIELDS ------------------------------

    private static final String PICTURE_URL = "PICTURE_URL";

// -------------------------- STATIC METHODS --------------------------

    public static Fragment newInstance(String pictureUrl) {
        Bundle args = new Bundle();
        args.putString(PICTURE_URL, pictureUrl);

        Fragment fragment = new PictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MirrorApp.refWatcher(getActivity()).watch(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        GlidePalette palette = GlidePalette
                .with(getArguments().getString(PICTURE_URL))
                .use(GlidePalette.Profile.VIBRANT)
                .intoBackground(view.findViewById(R.id.picture_container));
        Glide.with(getActivity())
                .load(getArguments().getString(PICTURE_URL))
                .listener(palette)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into((ImageView) view.findViewById(R.id.picture_view));
    }
}