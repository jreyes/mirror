package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.percent.PercentFrameLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.vaporwarecorp.mirror.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class DottedGridView extends FrameLayout {
// ------------------------------ FIELDS ------------------------------

    private Drawable mBorder;
    private View mContainer;
    private Drawable mBackground;

// --------------------------- CONSTRUCTORS ---------------------------

    public DottedGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackground(mBorder);
                mContainer.setBackground(mBackground);
                break;

            case MotionEvent.ACTION_UP:
                setBackground(null);
                mContainer.setBackground(null);
                break;

            default:
                return false;
        }

        return true;
    }

    private void initLayout(Context context) {
        mBorder = ContextCompat.getDrawable(context, R.drawable.bg_solid_border);
        mBackground = ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid);

        mContainer = new PercentFrameLayout(context);
        mContainer.setLayoutParams(new PercentFrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        addView(mContainer);
    }
}
