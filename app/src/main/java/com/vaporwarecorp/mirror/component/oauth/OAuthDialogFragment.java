package com.vaporwarecorp.mirror.component.oauth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.api.client.http.GenericUrl;
import com.vaporwarecorp.mirror.component.oauth.implicit.ImplicitResponseUrl;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import timber.log.Timber;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class OAuthDialogFragment extends DialogFragment {
// ------------------------------ FIELDS ------------------------------

    private static final String ARG_AUTHORIZATION_REQUEST_URL = "authRequestUrl";
    private static final String ARG_AUTHORIZATION_TYPE = "authType";
    private static final String AUTHORIZATION_IMPLICIT = "implicit";

    private AuthorizationDialogController mController;

// -------------------------- STATIC METHODS --------------------------

    static boolean isRedirectUriFound(String uri, String redirectUri) {
        Uri u;
        Uri r;
        try {
            u = Uri.parse(uri);
            r = Uri.parse(redirectUri);
        } catch (NullPointerException e) {
            return false;
        }
        if (u == null || r == null) {
            return false;
        }
        boolean rOpaque = r.isOpaque();
        boolean uOpaque = u.isOpaque();
        if (rOpaque != uOpaque) {
            return false;
        }
        if (rOpaque) {
            return TextUtils.equals(uri, redirectUri);
        }
        if (!TextUtils.equals(r.getScheme(), u.getScheme())) {
            return false;
        }
        if (!TextUtils.equals(r.getAuthority(), u.getAuthority())) {
            return false;
        }
        if (r.getPort() != u.getPort()) {
            return false;
        }
        if (!TextUtils.isEmpty(r.getPath()) && !TextUtils.equals(r.getPath(), u.getPath())) {
            return false;
        }
        Set<String> paramKeys = CompatUri.getQueryParameterNames(r);
        for (String key : paramKeys) {
            if (!TextUtils.equals(r.getQueryParameter(key), u.getQueryParameter(key))) {
                return false;
            }
        }
        String frag = r.getFragment();
        return !(!TextUtils.isEmpty(frag) && !TextUtils.equals(frag, u.getFragment()));
    }

    public static OAuthDialogFragment newInstance(
            GenericUrl authorizationRequestUrl,
            DialogFragmentController controller) {
        Bundle args = new Bundle();
        args.putString(ARG_AUTHORIZATION_REQUEST_URL, authorizationRequestUrl.build());
        args.putString(ARG_AUTHORIZATION_TYPE, AUTHORIZATION_IMPLICIT);

        OAuthDialogFragment frag = new OAuthDialogFragment();
        frag.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        frag.setArguments(args);
        frag.setController(controller);
        return frag;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnCancelListener ---------------------

    @Override
    public void onCancel(DialogInterface dialog) {
        onError(AuthorizationDialogController.ERROR_USER_CANCELLED);
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WebView wv = (WebView) getView().findViewById(android.R.id.primary);
        if (wv != null) {
            wv.loadUrl(getArguments().getString(ARG_AUTHORIZATION_REQUEST_URL));
        }
        getDialog().getWindow().setLayout(MATCH_PARENT, MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(null);

        View divider = getDialog().findViewById(getDialog().getContext().getResources()
                .getIdentifier("android:id/titleDivider", null, null));
        if (divider != null) {
            divider.setBackgroundColor(getDialog().getContext().getResources().getColor(
                    android.R.color.background_dark));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        mController.onPrepareDialog(dialog);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View customLayout = mController.onCreateView(inflater, container, savedInstanceState);
        if (customLayout != null) {
            return customLayout;
        }

        final Context context = inflater.getContext();

        FrameLayout root = new FrameLayout(context);
        root.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        WebView wv = new WebView(context);
        wv.setId(android.R.id.primary);

        root.addView(wv, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        LinearLayout pframe = new LinearLayout(context);
        pframe.setId(android.R.id.widget_frame);
        pframe.setOrientation(LinearLayout.VERTICAL);
        pframe.setVisibility(View.GONE);
        pframe.setGravity(Gravity.CENTER);

        ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        progress.setId(android.R.id.progress);
        pframe.addView(progress, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        TextView progressText = new TextView(context, null, android.R.attr.textViewStyle);
        progressText.setId(android.R.id.text1);
        pframe.addView(progressText, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        root.addView(pframe, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        return root;
    }

    @Override
    public void onDestroy() {
        setController(null);
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View rawWebView = view.findViewById(android.R.id.primary);
        if (rawWebView == null) {
            throw new RuntimeException(
                    "Your content must have a WebView whose id attribute is " +
                            "'android.R.id.primary'");
        }
        if (!(rawWebView instanceof WebView)) {
            throw new RuntimeException(
                    "Content has view with id attribute 'android.R.id.primary' "
                            + "that is not a WebView class");
        }
        WebView wv = (WebView) rawWebView;
        WebSettings webSettings = wv.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);

        wv.setOnKeyListener((v, keyCode, event) -> {
            WebView wv1 = (WebView) v;
            if (keyCode == KeyEvent.KEYCODE_BACK && wv1.canGoBack()) {
                wv1.goBack();
                return true;
            }
            return false;
        });

        if (mController.isJavascriptEnabledForWebView()) {
            webSettings.setJavaScriptEnabled(true);
        }

        if (mController.disableWebViewCache()) {
            webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        if (mController.removePreviousCookie()) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress != 0 && newProgress != 100) {
                    setProgressShown(view.getUrl(), getView(), newProgress);
                }
            }
        });

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Timber.i("shouldOverrideUrlLoading: " + url);
                interceptUrlCompat(view, url, true);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Timber.i("onPageStarted: " + url);
                if (!interceptUrlCompat(view, url, false)) {
                    setProgressShown(url, getView(), 0);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setProgressShown(url, getView(), 100);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                onError(description);
            }

            private boolean interceptUrlCompat(WebView view, String url, boolean loadUrl) {
                if (!isAdded() || isRemoving() || mController == null) {
                    return false;
                }
                String redirectUri;
                try {
                    redirectUri = mController.getRedirectUri();
                } catch (IOException e) {
                    onError(e.getMessage());
                    return false;
                }

                Timber.i("url: " + url + ", redirect: " + redirectUri + ", callback: "
                        + isRedirectUriFound(url, redirectUri));
                if (isRedirectUriFound(url, redirectUri)) {
                    // implicit
                    ImplicitResponseUrl implicitResponseUrl = new ImplicitResponseUrl(url);
                    String error = implicitResponseUrl.getError();
                    if (!TextUtils.isEmpty(error)
                            && !TextUtils.isEmpty(implicitResponseUrl.getErrorDescription())) {
                        error += (": " + implicitResponseUrl.getErrorDescription());
                    }
                    mController.set(implicitResponseUrl.getAccessToken(), error,
                            implicitResponseUrl, true);
                    return true;
                }
                if (loadUrl) {
                    view.loadUrl(url);
                }
                return false;
            }
        });
    }

    final void setController(AuthorizationDialogController controller) {
        mController = controller;
    }

    private void onError(String errorMessage) {
        if (mController != null) {
            mController.set(null, errorMessage, null, true);
        }
    }

    private void setProgressShown(String url, View view, int newProgress) {
        boolean handled = false;
        if (mController != null) {
            handled = mController.setProgressShown(url, view, newProgress);
        }
        View progress = null;
        View progressbar = null;
        if (!handled) {
            if (view != null) {
                progress = view.findViewById(android.R.id.text1);
                progressbar = view.findViewById(android.R.id.progress);
                view = view.findViewById(android.R.id.widget_frame);
            }
            if (view != null) {
                if (progress != null && progress instanceof TextView) {
                    ((TextView) progress).setText(newProgress + "%");
                }
                if (progressbar != null && progressbar instanceof ProgressBar) {
                    if (newProgress > 0 && newProgress < 100) {
                        ((ProgressBar) progressbar).setIndeterminate(false);
                    }
                    ((ProgressBar) progressbar).setProgress(newProgress);
                }
                view.setVisibility(newProgress != 100 ? View.VISIBLE : View.GONE);
            }
        }
    }
}
