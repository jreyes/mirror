package com.vaporwarecorp.mirror.component;

import android.content.Intent;

public interface WebAuthentication {
// -------------------------- OTHER METHODS --------------------------

    void doAuthentication();

    void isAuthenticated(IsAuthenticatedCallback callback);

    void onAuthenticationResult(int requestCode, int resultCode, Intent data);

    interface DoAuthenticationCallback {
        void onResult(boolean result);
    }

    interface IsAuthenticatedCallback {
        void onResult(boolean result);
    }
}
