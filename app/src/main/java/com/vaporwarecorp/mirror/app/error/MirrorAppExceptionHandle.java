package com.vaporwarecorp.mirror.app.error;

import com.robopupu.api.app.AppExceptionHandler;

public class MirrorAppExceptionHandle extends AppExceptionHandler<MirrorAppError> {
// --------------------------- CONSTRUCTORS ---------------------------

    private MirrorAppExceptionHandle() {
        super(null);
    }

    @Override
    protected MirrorAppError getAppSpecificUnknownError() {
        return MirrorAppError.ERROR_UNKNOWN;
    }
}