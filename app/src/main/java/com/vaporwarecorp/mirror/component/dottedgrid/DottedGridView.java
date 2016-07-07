/*
 * Copyright 2016 Johann Reyes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.component.dottedgrid;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.vaporwarecorp.mirror.R;

import java.util.LinkedHashSet;
import java.util.Set;

import static android.support.v4.widget.ViewDragHelper.STATE_IDLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static solid.stream.Stream.stream;

public class DottedGridView extends FrameLayout {
// ------------------------------ FIELDS ------------------------------

    private static final int INVALID_POINTER = -1;
    private static final int MAX_OVERFLOW = 300;
    private static final int MIN_OVERFLOW = -100;
    private static final float SENSITIVITY = 1f;

    @SuppressWarnings("WeakerAccess")
    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public void onViewDragStateChanged(int state) {
            if (state == STATE_IDLE && mDraggedView != null) {
                mDraggedView.hideBorder();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView != mDraggedView) {
                return;
            }

            BorderView borderView = (BorderView) changedView;

            // let's scale on drag
            if (borderView.isMaximized()) {
                borderView.scale(left, top);
            }

            final int newLeft = left + mColumnLeftPadding;
            final int newRight = getWidth() - left - mColumnRightPadding;
            if (newLeft < MIN_OVERFLOW || newRight < MIN_OVERFLOW) {
                borderView.showBorderWarning();
            } else {
                borderView.showBorder();
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            BorderView borderView = (BorderView) releasedChild;

            final int left = borderView.getLeft() + mColumnLeftPadding;
            final int right = getWidth() - borderView.getRight() + mColumnLeftPadding;
            if (right < mColumnSizeSide) {
                if (right < MIN_OVERFLOW) {
                    notifyUpdateCloseOnRight(borderView);
                } else {
                    rearrangeRightContainer(borderView);
                }
            } else if (left < mColumnSizeSide) {
                if (left < MIN_OVERFLOW) {
                    notifyUpdateCloseOnLeft(borderView);
                } else {
                    rearrangeLeftContainer(borderView);
                }
            } else {
                rearrangeCenterContainer(borderView);
            }
            ViewCompat.postInvalidateOnAnimation(DottedGridView.this);
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
            if (mDraggedView == null) {
                return 0;
            }

            final int leftBound = -MAX_OVERFLOW - (mColumnSizeCenter / 2);
            final int rightBound = getWidth() + MAX_OVERFLOW;
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
            if (mDraggedView == null) {
                return 0;
            }

            final int viewMargin = Math.round(mDraggedView.getHeight() / 4);
            final int topBound = getPaddingTop() - viewMargin;
            final int bottomBound = getHeight() - mDraggedView.getHeight() + viewMargin;
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
            return mDraggedView != null;
        }
    };

    private FrameLayout mBackground;
    private int mColumnLeftPadding;
    private int mColumnRightPadding;
    private int mColumnSizeCenter;
    private int mColumnSizeSide;
    private BorderView mDraggedView;
    private Listener mListener;
    private ViewDragHelper mViewDragHelper;
    private Set<BorderView> mViews;

// --------------------------- CONSTRUCTORS ---------------------------

    public DottedGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(context);
        initializeViewDragHelper();
    }

// -------------------------- OTHER METHODS --------------------------

    public int addBorderView(final Context context) {
        // TODO find better place for this
        // column in center is 40% while side columns is 15% of layout
        // so distribution would be 17/17/32/17/17
        mColumnSizeCenter = Math.round(getWidth() / 2);
        mColumnSizeSide = Math.round(mColumnSizeCenter / 2);
        mColumnLeftPadding = Math.round(mColumnSizeCenter / 4);
        mColumnRightPadding = Math.round((mColumnSizeCenter / 4) * 3);

        BorderView borderView = BorderView.newInstance(context, mColumnSizeCenter);
        mViews.add(borderView);
        addView(borderView);
        return borderView.getId();
    }

    /**
     * Clear the DottedGridView from any view.
     */
    public void clear() {
        final BorderView[] views = mViews.toArray(new BorderView[mViews.size()]);
        for (BorderView borderView : views) {
            notifyUpdateCloseOnLeft(borderView);
        }
    }

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

    /**
     * Returns the ID of the view that is maximized of -1.
     */
    public int getMaximizedContainerId() {
        for (BorderView borderView : mViews) {
            if (borderView.isMaximized()) {
                return borderView.getId();
            }
        }
        return -1;
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

        mDraggedView = null;
        stream(mViews)
                .filter(v -> isViewHit(v, (int) ev.getX(), (int) ev.getY()))
                .forEach((BorderView v) -> mDraggedView = v);

        final int actionMasked = MotionEventCompat.getActionMasked(ev);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                hideDraggedViewBorder();
                mViewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                showDraggedViewBorder();
                mViewDragHelper.processTouchEvent(ev);
                return false;
            default:
                break;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev) ||
                mViewDragHelper.isViewUnder(mDraggedView, (int) ev.getX(), (int) ev.getY());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int actionMasked = MotionEventCompat.getActionMasked(ev);
        switch (actionMasked & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                hideDraggedViewBorder();
                break;
            case MotionEvent.ACTION_DOWN:
                showDraggedViewBorder();
                if (MotionEventCompat.getPointerId(ev, actionMasked) == INVALID_POINTER) {
                    return false;
                }
                break;
        }
        mViewDragHelper.processTouchEvent(ev);
        return true;
    }

    public void removeBorderView(final int viewId) {
        // remove the view
        // now rearrange the layout that this view was removed from
        BorderView borderView = null;
        for (BorderView view : mViews) {
            if (view.getId() == viewId) {
                borderView = view;
            }
        }
        if (borderView != null) {
            // remove the view
            mViews.remove(borderView);
            removeView(borderView);

            // now rearrange the layout that this view was removed from
            if (borderView.isLeftAligned()) {
                rearrangeLeftContainer();
            }
            if (borderView.isRightAligned()) {
                rearrangeRightContainer();
            }
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void hideDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.hideBorder();
        }
        mBackground.setVisibility(INVISIBLE);
    }

    private void initializeLayout(Context context) {
        mViews = new LinkedHashSet<>();

        FrameLayout gridLayout = new FrameLayout(context);
        gridLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dotted_grid));
        gridLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mBackground = new FrameLayout(context);
        mBackground.addView(gridLayout);
        mBackground.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_solid_border));
        mBackground.setVisibility(INVISIBLE);
        addView(mBackground);

        // now set the child clipping to false
        setClipChildren(false);
    }

    /**
     * Initialize the viewDragHelper.
     */
    private void initializeViewDragHelper() {
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, mCallback);
    }

    /**
     * Calculate if one position is above any view.
     *
     * @param view to analyze.
     * @param x    position.
     * @param y    position.
     * @return true if x and y positions are below the view.
     */
    private boolean isViewHit(BorderView view, int x, int y) {
        int widthPadding = 0;
        int heightPadding = 0;
        if (!view.isMaximized()) {
            widthPadding = Math.round(view.getWidth() / 4);
            heightPadding = Math.round(view.getHeight() / 4);
        }
        return view.getLeft() + widthPadding < x
                && view.getRight() - widthPadding > x
                && view.getTop() + heightPadding < y
                && view.getBottom() - heightPadding > y;
    }

    private void notifyUpdateCloseOnLeft(BorderView borderView) {
        if (mListener != null) {
            mListener.onClosedToLeft(borderView.getId());
        }
    }

    private void notifyUpdateCloseOnRight(BorderView borderView) {
        if (mListener != null) {
            mListener.onClosedToRight(borderView.getId());
        }
    }

    private void notifyUpdateViewOnCenter(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnCenter(borderView.getId());
        }
    }

    private void notifyUpdateViewOnLeft(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnLeft(borderView.getId());
        }
    }

    private void notifyUpdateViewOnRight(BorderView borderView) {
        if (mListener != null) {
            mListener.onViewOnRight(borderView.getId());
        }
    }

    private void rearrangeCenterContainer(BorderView borderView) {
        borderView.maximize();
        notifyUpdateViewOnCenter(borderView);

        rearrangeLeftContainer();
        rearrangeRightContainer();
    }

    private void rearrangeContainer(int newLeft, boolean isLeftAligned) {
        int newTop = 0;
        for (BorderView borderView : mViews) {
            if (isLeftAligned ? borderView.isLeftAligned() : borderView.isRightAligned()) {
                LayoutParams params = (LayoutParams) borderView.getLayoutParams();
                newTop = newTop - Math.round(borderView.getHeight() / 4);
                if (params.leftMargin != newLeft || params.topMargin != newTop) {
                    params.leftMargin = newLeft;
                    params.topMargin = newTop;
                    if (mViewDragHelper.smoothSlideViewTo(borderView, newLeft, newTop)) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                if (newTop < 0) {
                    newTop += Math.round((borderView.getHeight() / 4) * 3);
                } else {
                    newTop += Math.round(borderView.getHeight() / 2);
                }
            }
        }
    }

    private void rearrangeContainer(BorderView borderView, int newLeft, boolean isLeftAligned) {
        // if it is already left aligned, don't realigned it
        if (isLeftAligned ? borderView.isLeftAligned() : borderView.isRightAligned()) {
            LayoutParams params = (LayoutParams) borderView.getLayoutParams();
            if (mViewDragHelper.smoothSlideViewTo(borderView, params.leftMargin, params.topMargin)) {
                ViewCompat.postInvalidateOnAnimation(this);
                return;
            }
        }

        // let's iterate the views
        int newTop = 0;
        for (BorderView view : mViews) {
            if (view.getId() != borderView.getId() && isLeftAligned && view.isLeftAligned()) {
                newTop += Math.round(view.getHeight() / 2);
            }
            if (view.getId() != borderView.getId() && !isLeftAligned && view.isRightAligned()) {
                newTop += Math.round(view.getHeight() / 2);
            }
        }
        newTop = newTop - Math.round(borderView.getHeight() / 4);
        borderView.minimize(isLeftAligned, newLeft, newTop);

        if (isLeftAligned) {
            // set BorderView to left aligned
            notifyUpdateViewOnLeft(borderView);
            // and rearrange the right side in case this view came from it
            rearrangeRightContainer();
        } else {
            // set BorderView to right aligned
            notifyUpdateViewOnRight(borderView);
            // and rearrange the left side in case this view came from it
            rearrangeLeftContainer();
        }
    }

    private void rearrangeLeftContainer() {
        int newLeft = 0 - mColumnLeftPadding;
        rearrangeContainer(newLeft, true);
    }

    private void rearrangeLeftContainer(BorderView borderView) {
        int newLeft = 0 - mColumnLeftPadding;
        rearrangeContainer(borderView, newLeft, true);
    }

    private void rearrangeRightContainer() {
        int newLeft = getWidth() - mColumnRightPadding;
        rearrangeContainer(newLeft, false);
    }

    private void rearrangeRightContainer(BorderView borderView) {
        int newLeft = getWidth() - mColumnRightPadding;
        rearrangeContainer(borderView, newLeft, false);
    }

    private void showDraggedViewBorder() {
        if (mDraggedView != null) {
            mDraggedView.bringToFront();
            mDraggedView.showBorder();
        }
        mBackground.setVisibility(VISIBLE);
    }

// -------------------------- INNER CLASSES --------------------------

    @SuppressWarnings("unused")
    public interface Listener {
        /**
         * Called when the view is closed to the right.
         */
        void onClosedToRight(int containerId);

        /**
         * Called when the view is closed to the left.
         */
        void onClosedToLeft(int containerId);

        /**
         * Called when the view is set in the center
         */
        void onViewOnCenter(int containerId);

        /**
         * Called when the view is set on the left
         */
        void onViewOnLeft(int containerId);

        /**
         * Called when the view is set on the right
         */
        void onViewOnRight(int containerId);
    }
}
