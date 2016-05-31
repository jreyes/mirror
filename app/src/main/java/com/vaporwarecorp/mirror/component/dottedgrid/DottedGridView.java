package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.vaporwarecorp.mirror.R;

import timber.log.Timber;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.vaporwarecorp.mirror.util.DisplayMetricsUtil.convertDpToPixel;

public class DottedGridView extends PercentRelativeLayout {
// ------------------------------ FIELDS ------------------------------

    private static final int INVALID_POINTER = -1;
    private static final float SENSITIVITY = 1f;

    private int activePointerId = INVALID_POINTER;
    private FrameLayout mBackground;
    private FrameLayout mFragmentContainer;
    private ViewDragHelper mViewDragHelper;

// --------------------------- CONSTRUCTORS ---------------------------

    public DottedGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(context);
        initializeViewDragHelper();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * To ensure the animation is going to work this method has been override to call
     * postInvalidateOnAnimation if the view is not settled yet.
     */
    @Override
    public void computeScroll() {
        if (!isInEditMode() && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public int getDraggedViewHeightPlusMarginTop() {
        return mDraggedView.getHeight();
    }

    /**
     * Checks if the top view is closed at the right or left place.
     *
     * @return true if the view is closed.
     */
    public boolean isClosed() {
        return isClosedAtLeft() || isClosedAtRight();
    }

    /**
     * Checks if the top view is closed at the left place.
     *
     * @return true if the view is closed at left.
     */
    public boolean isClosedAtLeft() {
        return mDraggedView.getRight() <= 0;
    }

    /**
     * Checks if the top view closed at the right place.
     *
     * @return true if the view is closed at right.
     */
    public boolean isClosedAtRight() {
        return mDraggedView.getLeft() >= getWidth();
    }

    /**
     * Override method to intercept only touch events over the drag view and to cancel the drag when
     * the action associated to the MotionEvent is equals to ACTION_CANCEL or ACTION_UP.
     *
     * @param ev captured.
     * @return true if the view is going to process the touch event or false if not.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        View view = findViewById(MotionEventCompat.getSource(ev));
        if (view != null && view instanceof BorderView) {
            mDraggedView = (BorderView) view;
        }
        Timber.d("onInterceptTouchEvent");
        switch (MotionEventCompat.getActionMasked(ev) & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mViewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                break;
            default:
                break;
        }
        boolean interceptTap = mDraggedView == null ||
                mViewDragHelper.isViewUnder(mDraggedView, (int) ev.getX(), (int) ev.getY());
        return mViewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
    }

    private void showDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.showBorder();
        }
    }

    private void hideDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.hideBorder();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Timber.d("onTouchEvent");
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_UP:
                hideDraggedViewBorder();
                mBackground.setVisibility(INVISIBLE);
                break;
            case MotionEvent.ACTION_DOWN:
                showDraggedViewBorder();
                activePointerId = MotionEventCompat.getPointerId(event, actionMasked);
                mBackground.setVisibility(VISIBLE);
                break;
        }
        if (activePointerId == INVALID_POINTER) {
            return false;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private void initializeLayout(Context context) {
        mBorderPadding = Math.round(convertDpToPixel(2, context));

        FrameLayout gridLayout = new FrameLayout(context);
        gridLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid));
        gridLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mBackground = new FrameLayout(context);
        mBackground.addView(gridLayout);
        mBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_solid_border));
        mBackground.setBottom(0);
        mBackground.setLeft(0);
        mBackground.setRight(0);
        mBackground.setTop(0);
        mBackground.setVisibility(INVISIBLE);
        addView(mBackground);

        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.addRule(PercentRelativeLayout.CENTER_IN_PARENT);
        params.getPercentLayoutInfo().widthPercent = 0.3f;

        mFragmentContainer = new BorderView(context);
        mFragmentContainer.setId(R.id.fragment_container);
        //mFragmentContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        mFragmentContainer.setLayoutParams(params);
        addView(mFragmentContainer);
    }

    /**
     * Initialize the viewDragHelper.
     */
    private void initializeViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, mCallback);
    }

    private BorderView mDraggedView;
    private int mBorderPadding;

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            mDraggedView.setLeft(releasedChild.getLeft());
            mDraggedView.setTop(releasedChild.getTop());
        }

        /**
         * Override method used to configure the horizontal drag. Restrict the motion of the dragged
         * child view along the horizontal axis.
         *
         * @param child child view being dragged.
         * @param left  attempted motion along the X axis.
         * @param dx    proposed change in position for left.
         * @return the new clamped position for left.
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft() + mBorderPadding;
            final int rightBound = getWidth() - mDraggedView.getWidth() - mBorderPadding;
            return Math.min(Math.max(left, leftBound), rightBound);
        }

        /**
         * Override method used to configure the vertical drag. Restrict the motion of the dragged child
         * view along the vertical axis.
         *
         * @param child child view being dragged.
         * @param top   attempted motion along the Y axis.
         * @param dy    proposed change in position for top.
         * @return the new clamped position for top.
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop() + mBorderPadding;
            final int bottomBound = getHeight() - mDraggedView.getHeight() - mBorderPadding;
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        /**
         * Override method used to configure which is going to be the dragged view.
         *
         * @param view      child the user is attempting to capture.
         * @param pointerId ID of the pointer attempting the capture,
         * @return true if capture should be allowed, false otherwise.
         */
        @Override
        public boolean tryCaptureView(View view, int pointerId) {
            Timber.d("tryCaptureView");
            if (view instanceof BorderView) {
                mDraggedView = (BorderView) view;
                return true;
            } else {
                mDraggedView = null;
                return false;
            }
        }
    };
}
