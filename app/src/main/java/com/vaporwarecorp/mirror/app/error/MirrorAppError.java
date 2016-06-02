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