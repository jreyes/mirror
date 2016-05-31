package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.vaporwarecorp.mirror.R;

public class BorderView extends FrameLayout {
// ------------------------------ FIELDS ------------------------------

    private Drawable mBorder;

// --------------------------- CONSTRUCTORS ---------------------------

    public BorderView(Context context) {
        this(context, null);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setBackground(mBorder);
                break;

            case MotionEvent.ACTION_UP:
                setBackground(null);
                return false;
        }
        return true;
    }

    private void initializeLayout(Context context) {
        mBorder = ContextCompat.getDrawable(context, R.drawable.bg_solid_border);
    }
}
