package com.vaporwarecorp.mirror.component.datetime;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.vaporwarecorp.mirror.R;

public class DateTimeView extends LinearLayout {
// --------------------------- CONSTRUCTORS ---------------------------

    public DateTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_date_time, this, true);
    }
}
