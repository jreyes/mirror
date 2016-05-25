package com.vaporwarecorp.mirror.component.datetime;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.vaporwarecorp.mirror.R;

public class DateTimeView extends RelativeLayout {
// --------------------------- CONSTRUCTORS ---------------------------

    public DateTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_date_time, this);
    }
}
