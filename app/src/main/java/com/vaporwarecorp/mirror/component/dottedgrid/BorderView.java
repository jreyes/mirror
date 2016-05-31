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

import static com.vaporwarecorp.mirror.util.DisplayMetricsUtil.convertDpToPixel;

public class BorderView extends FrameLayout {

// --------------------------- CONSTRUCTORS ---------------------------

    public BorderView(Context context) {
        this(context, null);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeLayout(context);
    }

    public void showBorder() {
        setBackgroundColor(mColorWhite);
    }

    public void hideBorder() {
        setBackgroundColor(mColorTransparent);
    }

    private int mColorWhite;
    private int mColorTransparent;

    private void initializeLayout(final Context context) {
        mColorTransparent = ContextCompat.getColor(context, android.R.color.transparent);
        mColorWhite = ContextCompat.getColor(context, android.R.color.white);

        final int borderPadding = Math.round(convertDpToPixel(2, context));
        setPadding(borderPadding, borderPadding, borderPadding, borderPadding);
    }
}
