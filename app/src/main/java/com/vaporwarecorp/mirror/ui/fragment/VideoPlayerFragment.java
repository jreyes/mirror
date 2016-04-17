package com.vaporwarecorp.mirror.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.event.HandWaveClickEvent;
import com.vaporwarecorp.mirror.event.HandWaveRightEvent;
import com.vaporwarecorp.mirror.manager.VideoManager;
import com.vaporwarecorp.mirror.vendor.ijk.IjkVideoView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VideoPlayerFragment extends Fragment {
// ------------------------------ FIELDS ------------------------------

    private static final String VIDEO_URI = "VIDEO_URI";

    private VideoManager mVideoManager;

// -------------------------- STATIC METHODS --------------------------

    public static Fragment newInstance(String videoUri) {
        Bundle args = new Bundle();
        args.putString(VIDEO_URI, videoUri);

        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_player, container, false);
    }

    @Override
    public void onDestroy() {
        mVideoManager.onDestroy();
        super.onDestroy();
        MirrorApp.refWatcher(getActivity()).watch(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveRightEvent event) {
        mVideoManager.stop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent(HandWaveClickEvent event) {
        mVideoManager.togglePlay();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        mVideoManager.stop();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        String videoUri = getArguments().getString(VIDEO_URI);
        mVideoManager = MirrorApp.video(getActivity(), (IjkVideoView) view.findViewById(R.id.video_view));
        mVideoManager.start(videoUri);
    }
}
