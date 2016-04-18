package com.vaporwarecorp.mirror.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.vaporwarecorp.mirror.MirrorApp;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.event.GreetGoodbyeEvent;
import com.vaporwarecorp.mirror.event.GreetWelcomeEvent;
import org.greenrobot.eventbus.EventBus;

public class GreeterFragment extends Fragment implements Animator.AnimatorListener {
// ------------------------------ FIELDS ------------------------------

    private static final String GREET_NAME = "GREET_NAME";
    private static final String GREET_TYPE = "GREET_TYPE";

    private Handler mHandler;

// -------------------------- STATIC METHODS --------------------------

    public static Fragment newInstance(String name, String type) {
        Bundle args = new Bundle();
        args.putString(GREET_NAME, name);
        args.putString(GREET_TYPE, type);

        GreeterFragment fragment = new GreeterFragment();
        fragment.setArguments(args);
        return fragment;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AnimatorListener ---------------------

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (isWelcome()) {
            mHandler.postDelayed(() -> EventBus.getDefault().post(new GreetWelcomeEvent()), 7000);
        } else {
            mHandler.postDelayed(() -> EventBus.getDefault().post(new GreetGoodbyeEvent()), 7000);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mHandler = new Handler();

        ((TextView) view.findViewById(R.id.greet_name_text)).setText(getArguments().getString(GREET_NAME));
        if (isWelcome()) {
            ((TextView) view.findViewById(R.id.greet_type_text)).setText(getString(R.string.greet_welcome));
            YoYo.with(Techniques.FadeIn)
                    .duration(1000)
                    .withListener(this)
                    .playOn(view.findViewById(R.id.greet_container));
        } else {
            ((TextView) view.findViewById(R.id.greet_type_text)).setText(getString(R.string.greet_goodbye));
            YoYo.with(Techniques.FadeIn)
                    .duration(1000)
                    .withListener(this)
                    .playOn(view.findViewById(R.id.greet_container));
        }
    }

    private boolean isWelcome() {
        return "WELCOME".equals(getArguments().getString(GREET_TYPE));
    }
}
