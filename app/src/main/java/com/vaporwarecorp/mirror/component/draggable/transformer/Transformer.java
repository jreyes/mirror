/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaporwarecorp.mirror.component.draggable.transformer;

import android.view.View;
import android.widget.RelativeLayout;
import com.nineoldandroids.view.ViewHelper;

/**
 * Abstract class created to be implemented by different classes are going to change the size of a
 * view. The most basic one is going to scale the view and the most complex used with VideoView is
 * going to change the size of the view.
 * <p/>
 * The view used in this class has to be contained by a RelativeLayout.
 * <p/>
 * This class also provide information about the size of the view and the position because
 * different Transformer implementations could change the size of the view but not the position,
 * like ScaleTransformer does.
 *
 * @author Pedro Vicente Gómez Sánchez
 */
public abstract class Transformer {
// ------------------------------ FIELDS ------------------------------

    private int marginBottom;
    private int marginRight;
    private int originalHeight;
    private int originalWidth;
    private final View parent;
    private final View view;
    private float xScaleFactor;
    private float yScaleFactor;

// --------------------------- CONSTRUCTORS ---------------------------

    public Transformer(View view, View parent) {
        this.view = view;
        this.parent = parent;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getMarginBottom() {
        return marginBottom;
    }

    public int getMarginRight() {
        return marginRight;
    }

    /**
     * @return height of the view before it has change the size.
     */
    public int getOriginalHeight() {
        if (originalHeight == 0) {
            originalHeight = view.getMeasuredHeight();
        }
        return originalHeight;
    }

    /**
     * @return width of the view before it has change the size.
     */
    public int getOriginalWidth() {
        if (originalWidth == 0) {
            originalWidth = view.getMeasuredWidth();
        }
        return originalWidth;
    }

    protected View getView() {
        return view;
    }

    public float getXScaleFactor() {
        return xScaleFactor;
    }

    public void setXScaleFactor(float xScaleFactor) {
        this.xScaleFactor = xScaleFactor;
    }

    public float getYScaleFactor() {
        return yScaleFactor;
    }

    public void setYScaleFactor(float yScaleFactor) {
        this.yScaleFactor = yScaleFactor;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * @return min possible height, after apply the transformation, plus the margin right.
     */
    public abstract int getMinHeightPlusMargin();

    /**
     * @return min possible width, after apply the transformation.
     */
    public abstract int getMinWidthPlusMarginRight();

    public boolean isAboveTheMiddle() {
        int parentHeight = parent.getHeight();
        float viewYPosition = ViewHelper.getY(view) + (view.getHeight() * 0.5f);
        return viewYPosition < (parentHeight * 0.5);
    }

    public abstract boolean isNextToLeftBound();

    public abstract boolean isNextToRightBound();

    public abstract boolean isViewAtBottom();

    public abstract boolean isViewAtRight();

    public boolean isViewAtTop() {
        return view.getTop() == 0;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = Math.round(marginBottom);
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = Math.round(marginRight);
    }

    /**
     * Change view height using the LayoutParams of the view.
     *
     * @param newHeight to change..
     */
    public void setViewHeight(int newHeight) {
        if (newHeight > 0) {
            originalHeight = newHeight;
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) view.getLayoutParams();
            layoutParams.height = newHeight;
            view.setLayoutParams(layoutParams);
        }
    }

    public abstract void updatePosition(float verticalDragOffset);

    public abstract void updateScale(float verticalDragOffset);

    protected View getParentView() {
        return parent;
    }
}