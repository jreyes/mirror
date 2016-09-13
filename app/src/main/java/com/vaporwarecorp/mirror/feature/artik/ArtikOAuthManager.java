/*
 *   Copyright 2016 Johann Reyes
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.vaporwarecorp.mirror.feature.artik;

import android.app.Activity;
import android.text.TextUtils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.vaporwarecorp.mirror.component.oauth.AuthorizationDialogController;
import com.vaporwarecorp.mirror.component.oauth.AuthorizationFlow;
import com.vaporwarecorp.mirror.component.oauth.DialogFragmentController;
import com.vaporwarecorp.mirror.component.oauth.OAuthManager;
import com.vaporwarecorp.mirror.component.oauth.store.SharedPreferencesCredentialStore;
import rx.Observable;

import java.io.IOException;

public class ArtikOAuthManager {
// ------------------------------ FIELDS ------------------------------

    private static final String AUTHORIZATION_URL = "https://accounts.artik.cloud/authorize";
    private static final String CREDENTIALS_STORE_PREF_FILE = "oauth";
    private static final String REDIRECT_URL = "mirror://artik/redirect";
    private static final String TOKEN_URL = "https://accounts.artik.cloud/token";
    private static final String USER_ID = "artik";

    private final OAuthManager manager;

// -------------------------- STATIC METHODS --------------------------

    public static ArtikOAuthManager newInstance(Activity activity, String clientId) {
        // create the ClientParametersAuthentication object
        final ClientParametersAuthentication client = new ClientParametersAuthentication(clientId, null);

        // create JsonFactory
        final JsonFactory jsonFactory = new JacksonFactory();

        // setup credential store
        final SharedPreferencesCredentialStore credentialStore =
                new SharedPreferencesCredentialStore(activity, CREDENTIALS_STORE_PREF_FILE, jsonFactory);

        // setup authorization flow
        AuthorizationFlow flow = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                AndroidHttp.newCompatibleTransport(),
                jsonFactory,
                new GenericUrl(TOKEN_URL),
                client,
                client.getClientId(),
                AUTHORIZATION_URL)
                //.setScopes(scopes)
                .setCredentialStore(credentialStore)
                .build();

        // setup authorization UI controller
        AuthorizationDialogController controller =
                new DialogFragmentController(activity.getFragmentManager()) {
                    @Override
                    public String getRedirectUri() throws IOException {
                        return REDIRECT_URL;
                    }

                    @Override
                    public boolean isJavascriptEnabledForWebView() {
                        return true;
                    }

                    @Override
                    public boolean disableWebViewCache() {
                        return false;
                    }

                    @Override
                    public boolean removePreviousCookie() {
                        return false;
                    }
                };
        return new ArtikOAuthManager(flow, controller);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private ArtikOAuthManager(AuthorizationFlow flow, AuthorizationDialogController controller) {
        Preconditions.checkNotNull(flow);
        Preconditions.checkNotNull(controller);
        this.manager = new OAuthManager(flow, controller);
    }

// -------------------------- OTHER METHODS --------------------------

    public Observable<String> authorizeImplicitly() {
        return Observable.create(subscriber -> {
            try {
                final Credential credential = manager.authorizeImplicitly(USER_ID, null, null).getResult();
                if (credential == null || TextUtils.isEmpty(credential.getAccessToken())) {
                    subscriber.onError(new NullPointerException());
                } else {
                    subscriber.onNext(credential.getAccessToken());
                }
            } catch (IOException e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        });
    }

    public OAuthManager.OAuthFuture<Boolean> deleteCredential() {
        return manager.deleteCredential(USER_ID, null, null);
    }
}
