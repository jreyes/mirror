package com.vaporwarecorp.mirror.component.command;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.vaporwarecorp.mirror.R;

public class CommandView extends RelativeLayout {
// ------------------------------ FIELDS ------------------------------

    private TextView mMicrophone;
    private boolean mMicrophoneOn;

// --------------------------- CONSTRUCTORS ---------------------------

    public CommandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_command, this);

        mMicrophone = (TextView) findViewById(R.id.microphone);
        if (!getRootView().isInEditMode()) {
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
            mMicrophone.setTypeface(font);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean isMicrophoneOn() {
        return mMicrophoneOn;
    }

    public void microphoneOff() {
        mMicrophoneOn = false;
        mMicrophone.setText(R.string.microphone_off);
    }

    public void microphoneOn() {
        mMicrophoneOn = true;
        mMicrophone.setText(R.string.microphone_on);
    }
}
