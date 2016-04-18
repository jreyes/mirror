package com.vaporwarecorp.mirror.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;

public class GreeterFragment extends Fragment {
// -------------------------- STATIC METHODS --------------------------

    public static Fragment newInstance() {
        GreeterFragment fragment = new GreeterFragment();
        return fragment;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_greet, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MirrorApp.refWatcher(getActivity()).watch(this);
    }
}
