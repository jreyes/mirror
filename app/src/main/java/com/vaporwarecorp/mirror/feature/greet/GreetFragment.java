package com.vaporwarecorp.mirror.feature.greet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.feature.FeatureFragment;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;

import static com.daimajia.androidanimations.library.Techniques.FadeIn;

@Plugin
public class GreetFragment extends FeatureFragment<GreetPresenter> implements GreetView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    GreetPresenter mPresenter;

    private View mGreetContainer;
    private TextView mGreetNameText;
    private TextView mGreetTypeText;

// --------------------------- CONSTRUCTORS ---------------------------

    @Provides(GreetView.class)
    public GreetFragment() {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface GreetView ---------------------

    @Override
    public void displayGreet(String greetName, boolean isWelcome) {
        mGreetTypeText.setText(getString(isWelcome ? R.string.greet_welcome : R.string.greet_goodbye));
        mGreetNameText.setText(greetName);
    }

// --------------------- Interface MirrorView ---------------------

    @Override
    public boolean isFullscreen() {
        return true;
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public GreetPresenter getPresenter() {
        return mPresenter;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle inState) {
        View view = inflater.inflate(R.layout.fragment_greet, container, false);
        mGreetContainer = view.findViewById(R.id.greet_container);
        mGreetTypeText = (TextView) view.findViewById(R.id.greet_type_text);
        mGreetNameText = (TextView) view.findViewById(R.id.greet_name_text);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        YoYo
                .with(FadeIn)
                .duration(2000)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mPresenter.onAnimationEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .playOn(mGreetContainer);
    }
}
