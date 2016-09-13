package com.vaporwarecorp.mirror.component.oauth;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;
import com.vaporwarecorp.mirror.component.oauth.implicit.ImplicitResponseUrl;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public interface AuthorizationDialogController {
// ------------------------------ FIELDS ------------------------------

    /**
     * The resource owner or authorization server denied the request.
     */
    String ERROR_ACCESS_DENIED = "access_denied";

    /**
     * The request is missing a required parameter, includes an invalid
     * parameter value, includes a parameter more than once, or is otherwise
     * malformed.
     */
    String ERROR_INVALID_REQUEST = "invalid_request";

    /**
     * The requested scope is invalid, unknown, or malformed.
     */
    String ERROR_INVALID_SCOPE = "invalid_scope";

    /**
     * The authorization server encountered an unexpected condition that
     * prevented it from fulfilling the request. (This error code is needed
     * because a 500 Internal Server Error HTTP status code cannot be returned
     * to the client via an HTTP redirect.)
     */
    String ERROR_SERVER_ERROR = "server_error";

    /**
     * The authorization server is currently unable to handle the request due to
     * a temporary overloading or maintenance of the server. (This error code is
     * needed because a 503 Service Unavailable HTTP status code cannot be
     * returned to the client via an HTTP redirect.)
     */
    String ERROR_TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";

    /**
     * The client is not authorized to request an authorization code using this
     * method.
     */
    String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";

    /**
     * The authorization server does not support obtaining an authorization code
     * using this method.
     */
    String ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";

    /**
     * Error indicating that the user has cancelled the authorization process,
     * most likely due to cancellation of the authorization dialog.
     */
    String ERROR_USER_CANCELLED = "user_cancelled";

// -------------------------- OTHER METHODS --------------------------

    /**
     * Indicate whether cache should be enabled for the WebView.
     *
     * @return {@code true} if cache should be enabled, {@code false}
     * otherwise.
     */
    boolean disableWebViewCache();

    /**
     * Returns the redirect URI.
     */
    String getRedirectUri() throws IOException;

    /**
     * Indicate whether Javascript should be enabled for the WebView.
     *
     * @return {@code true} if Javascript should be enabled, {@code false}
     * otherwise.
     */
    boolean isJavascriptEnabledForWebView();

    /**
     * Implement this method to supply the content of the dialog if needed.
     * Returning {@code null} will show the default implementation of dialog
     * content. Any customized implementation must include in its layout a
     * {@link WebView} object with the id {@link android.R.id#primary} and a
     * {@link View} object (most likely {@link ProgressBar}) with the id
     * {@link android.R.id#progress}.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState);

    /**
     * Prepares the dialog to be displayed. This is called before the dialog is
     * shown. You can use this method to modify the appearance of the dialog.
     *
     * @param dialog
     */
    void onPrepareDialog(Dialog dialog);

    /**
     * Indicate whether cookie should be removed before showing the WebView.
     *
     * @return {@code true} if cookie should be removed, {@code false}
     * otherwise.
     */
    boolean removePreviousCookie();

    /**
     * Handles user authorization by redirecting to the OAuth 2.0 authorization
     * server as defined in <a
     * href="http://tools.ietf.org/html/rfc6749#section-4.2.1">Authorization
     * Request</a>.
     *
     * @param authorizationRequestUrl
     */
    void requestAuthorization(BrowserClientRequestUrl authorizationRequestUrl);

    /**
     * Sets the result that are guarded by a {@link ReentrantLock}.
     *
     * @param codeOrToken
     * @param error
     * @param implicitResponseUrl
     * @param signal
     */
    void set(String codeOrToken, String error, ImplicitResponseUrl implicitResponseUrl,
             boolean signal);

    /**
     * Show the progress due to web page loading.
     *
     * @param url
     * @param view
     * @param progress page loading progress, represented by an integer between 0
     *                 and 100.
     * @return {@code true} if all UI is handled by the subclass implementation
     * and the default implementation of the parent class should be
     * bypassed, {@code false} otherwise.
     */
    boolean setProgressShown(String url, View view, int progress);

    /**
     * Releases any resources and stops any processes started.
     */
    void stop() throws IOException;

    /**
     * Waits for OAuth 2.0 implicit access token response.
     */
    ImplicitResponseUrl waitForImplicitResponseUrl() throws IOException;
}
