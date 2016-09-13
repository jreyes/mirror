package com.vaporwarecorp.mirror.component.oauth;

import android.app.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Preconditions;
import com.vaporwarecorp.mirror.component.oauth.implicit.ImplicitResponseUrl;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DialogFragmentController implements AuthorizationDialogController {
// ------------------------------ FIELDS ------------------------------

    private static final String FRAGMENT_TAG = "oauth_dialog";

    /**
     * Verification code (for explicit authorization) or access token (for
     * implicit authorization) or {@code null} for none.
     */
    private String codeOrToken;
    /**
     * Error code or {@code null} for none.
     */
    private String error;
    private final FragmentManager fragmentManager;
    /**
     * Condition for receiving an authorization response.
     */
    private final Condition gotAuthorizationResponse;
    /**
     * Implicit response URL.
     */
    private ImplicitResponseUrl implicitResponseUrl;
    /**
     * Lock on the code and error.
     */
    private final Lock lock;
    private final Handler uiHandler;

// --------------------------- CONSTRUCTORS ---------------------------

    public DialogFragmentController(FragmentManager fragmentManager) {
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.fragmentManager = Preconditions.checkNotNull(fragmentManager);

        this.lock = new ReentrantLock();
        this.gotAuthorizationResponse = lock.newCondition();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AuthorizationDialogController ---------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // use default implementation in DialogFragment
        return null;
    }

    @Override
    public void onPrepareDialog(Dialog dialog) {
        // do nothing
    }

    @Override
    public void set(String codeOrToken, String error, ImplicitResponseUrl implicitResponseUrl,
                    boolean signal) {
        lock.lock();
        try {
            this.error = error;
            this.codeOrToken = codeOrToken;
            this.implicitResponseUrl = implicitResponseUrl;
            if (signal) {
                gotAuthorizationResponse.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean setProgressShown(String url, View view, int newProgress) {
        // use default implementation in DialogFragment
        return false;
    }

// --------------------- Interface AuthorizationUIController ---------------------

    @Override
    public void requestAuthorization(BrowserClientRequestUrl authorizationRequestUrl) {
        internalRequestAuthorization(authorizationRequestUrl);
    }

    @Override
    public void stop() throws IOException {
        set(null, null, null, true);
        dismissDialog();
    }

    @Override
    public ImplicitResponseUrl waitForImplicitResponseUrl() throws IOException {
        lock.lock();
        try {
            while (codeOrToken == null && error == null) {
                gotAuthorizationResponse.awaitUninterruptibly();
            }
            dismissDialog();
            if (error != null) {
                if (TextUtils.equals(ERROR_USER_CANCELLED, error)) {
                    throw new CancellationException("User authorization failed (" + error + ")");
                } else {
                    throw new IOException("User authorization failed (" + error + ")");
                }
            }
            return implicitResponseUrl;
        } finally {
            lock.unlock();
        }
    }

    private void dismissDialog() {
        runOnMainThread(() -> {
            DialogFragment frag = (DialogFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
            if (frag != null) {
                frag.dismissAllowingStateLoss();
            }
        });
    }

    private void internalRequestAuthorization(final GenericUrl authorizationRequestUrl) {
        runOnMainThread(() -> {
            if (fragmentManager.isDestroyed()) {
                return;
            }

            DialogFragment frag = OAuthDialogFragment.newInstance(authorizationRequestUrl, DialogFragmentController.this);
            FragmentTransaction ft = fragmentManager.beginTransaction();
            Fragment prevDialog = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
            if (prevDialog != null) {
                ft.remove(prevDialog);
            }
            ft.add(frag, FRAGMENT_TAG).commitAllowingStateLoss();
        });
    }

    /**
     * Executes the {@link Runnable} on the main thread.
     *
     * @param runnable Runnable
     */
    private void runOnMainThread(Runnable runnable) {
        uiHandler.post(runnable);
    }
}
