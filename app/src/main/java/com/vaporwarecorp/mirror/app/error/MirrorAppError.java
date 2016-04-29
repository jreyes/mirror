package com.vaporwarecorp.mirror.app.error;

import android.content.Context;
import com.robopupu.api.app.AppError;
import com.vaporwarecorp.mirror.R;

public enum MirrorAppError implements AppError {
    ERROR_NONE(0, 0),

    // Native app errors

    ERROR_UNKNOWN(Integer.MAX_VALUE, R.string.error_unknown_error);

// ------------------------------ FIELDS ------------------------------

    private static Context sContext;
    private final int mCode;
    private final int mMessageFormat;

// -------------------------- STATIC METHODS --------------------------

    public static void setContext(final Context context) {
        sContext = context;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    MirrorAppError(final int code, final int messageFormat) {
        mCode = code;
        mMessageFormat = messageFormat;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AppError ---------------------

    public final int getCode() {
        return mCode;
    }

    @Override
    public String getMessage(final Object... args) {
        return sContext.getString(mMessageFormat, args);
    }
}