package com.vaporwarecorp.mirror.component.oauth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.CredentialStoreRefreshListener;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.vaporwarecorp.mirror.component.oauth.implicit.ImplicitResponseUrl;

import java.io.IOException;
import java.util.Collection;

/**
 * Thread-safe OAuth 1.0a and 2.0 authorization flow that manages and persists
 * end-user credentials. Both explicit authorization and implicit authorization
 * of OAuth 2.0 are supported.
 * <p>
 * This is designed to simplify the flow in which an end-user authorizes the
 * application to access their protected data, and then the application has
 * access to their data based on an access token and a refresh token to refresh
 * that access token when it expires.
 * </p>
 * <p>
 * The first step is to call {@link #loadCredential(String)} based on the known
 * user ID to check if the end-user's credentials are already known. If not,
 * call {@link #newImplicitAuthorizationUrl()}) and direct the end-user's browser to
 * an authorization page. If explicit authorization is used, the web browser
 * will then redirect to the redirect URL with a {@code "code"} query parameter
 * which can then be used to request an access token using
 * {@link #newTokenRequest(String)}; If implicit authorization is used, the web
 * browser will then redirect to the redirect URL with a {@code "access_token"}
 * fragment. The implicit redirect URL is returned as
 * {@link ImplicitResponseUrl}. Finally, use
 * {@link #createAndStoreCredential(TokenResponse, String)} or
 * {@link #createAndStoreCredential(ImplicitResponseUrl, String)} to store and
 * obtain a credential for accessing protected resources.
 * </p>
 *
 * @author David Wu
 */
public class AuthorizationFlow extends AuthorizationCodeFlow {
// ------------------------------ FIELDS ------------------------------

    /**
     * Credential created listener or {@code null} for none.
     */
    private final CredentialCreatedListener credentialCreatedListener;


// --------------------------- CONSTRUCTORS ---------------------------

    AuthorizationFlow(Builder builder) {
        super(builder);
        credentialCreatedListener = builder.getGeneralCredentialCreatedListener();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Creates a new credential for the given user ID based on the given token
     * response and store in the credential store.
     *
     * @param implicitResponse implicit authorization token response
     * @param userId           user ID or {@code null} if not using a persisted credential
     *                         store
     * @return newly created credential
     * @throws IOException
     */
    public Credential createAndStoreCredential(ImplicitResponseUrl implicitResponse, String userId)
            throws IOException {
        Credential credential = newCredential(userId)
                .setAccessToken(implicitResponse.getAccessToken())
                .setExpiresInSeconds(implicitResponse.getExpiresInSeconds());
        CredentialStore credentialStore = getCredentialStore();
        if (credentialStore != null) {
            credentialStore.store(userId, credential);
        }
        if (credentialCreatedListener != null) {
            credentialCreatedListener.onCredentialCreated(credential, implicitResponse);
        }
        return credential;
    }

    /**
     * Returns a new instance of an implicit authorization request URL.
     * <p>
     * This is a builder for an authorization web page to allow the end user to
     * authorize the application to access their protected resources and that
     * returns an access token. It uses the
     * {@link #getAuthorizationServerEncodedUrl()}, {@link #getClientId()}, and
     * {@link #getScopes()}. Sample usage:
     * </p>
     * <p>
     * <pre>
     * private AuthorizationFlow flow;
     *
     * public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
     *     String url = flow.newImplicitAuthorizationUrl().setState(&quot;xyz&quot;)
     *             .setRedirectUri(&quot;https://client.example.com/rd&quot;).build();
     *     response.sendRedirect(url);
     * }
     * </pre>
     */
    public BrowserClientRequestUrl newImplicitAuthorizationUrl() {
        return new BrowserClientRequestUrl(getAuthorizationServerEncodedUrl(), getClientId())
                .setScopes(getScopes());
    }

    /**
     * Returns a new OAuth 2.0 credential instance based on the given user ID.
     *
     * @param userId user ID or {@code null} if not using a persisted credential
     *               store
     */
    private Credential newCredential(String userId) {
        Credential.Builder builder = new Credential.Builder(getMethod())
                .setTransport(getTransport())
                .setJsonFactory(getJsonFactory())
                .setTokenServerEncodedUrl(getTokenServerEncodedUrl())
                .setClientAuthentication(getClientAuthentication())
                .setRequestInitializer(getRequestInitializer())
                .setClock(getClock());
        if (getCredentialStore() != null) {
            builder.addRefreshListener(
                    new CredentialStoreRefreshListener(userId, getCredentialStore()));
        }

        builder.getRefreshListeners().addAll(getRefreshListeners());

        return builder.build();
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Authorization flow builder.
     * <p>
     * Implementation is not thread-safe.
     * </p>
     */
    public static class Builder extends
            com.google.api.client.auth.oauth2.AuthorizationCodeFlow.Builder {
        /**
         * Credential created listener or {@code null} for none.
         */
        CredentialCreatedListener credentialCreatedListener;

        /**
         * @param method                        method of presenting the access token to the resource
         *                                      server (for example
         *                                      {@link BearerToken#authorizationHeaderAccessMethod})
         * @param transport                     HTTP transport
         * @param jsonFactory                   JSON factory
         * @param tokenServerUrl                token server URL
         * @param clientAuthentication          client authentication or {@code null} for
         *                                      none (see
         *                                      {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}
         *                                      )
         * @param clientId                      client identifier
         * @param authorizationServerEncodedUrl authorization server encoded URL
         */
        public Builder(AccessMethod method,
                       HttpTransport transport,
                       JsonFactory jsonFactory,
                       GenericUrl tokenServerUrl,
                       HttpExecuteInterceptor clientAuthentication,
                       String clientId,
                       String authorizationServerEncodedUrl) {
            super(method,
                    transport,
                    jsonFactory,
                    tokenServerUrl,
                    clientAuthentication,
                    clientId,
                    authorizationServerEncodedUrl);
        }

        /**
         * Returns a new instance of an authorization flow based on this
         * builder.
         */
        public AuthorizationFlow build() {
            return new AuthorizationFlow(this);
        }

        @Override
        public Builder setMethod(AccessMethod method) {
            return (Builder) super.setMethod(method);
        }

        @Override
        public Builder setTransport(HttpTransport transport) {
            return (Builder) super.setTransport(transport);
        }

        @Override
        public Builder setJsonFactory(JsonFactory jsonFactory) {
            return (Builder) super.setJsonFactory(jsonFactory);
        }

        @Override
        public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
            return (Builder) super.setTokenServerUrl(tokenServerUrl);
        }

        @Override
        public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
            return (Builder) super.setClientAuthentication(clientAuthentication);
        }

        @Override
        public Builder setClientId(String clientId) {
            return (Builder) super.setClientId(clientId);
        }

        @Override
        public Builder setAuthorizationServerEncodedUrl(String authorizationServerEncodedUrl) {
            return (Builder) super.setAuthorizationServerEncodedUrl(authorizationServerEncodedUrl);
        }

        @Override
        public Builder setClock(Clock clock) {
            return (Builder) super.setClock(clock);
        }

        @Beta
        @Override
        public Builder setCredentialStore(CredentialStore credentialStore) {
            return (Builder) super.setCredentialStore(credentialStore);
        }

        @Override
        public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
            return (Builder) super.setRequestInitializer(requestInitializer);
        }

        @Override
        public Builder setScopes(Collection<String> scopes) {
            return (Builder) super.setScopes(scopes);
        }

        /**
         * Sets the credential created listener or {@code null} for none. *
         * <p>
         * Overriding is only supported for the purpose of calling the super
         * implementation and changing the return type, but nothing else.
         * </p>
         */
        public Builder setCredentialCreatedListener(
                CredentialCreatedListener credentialCreatedListener) {
            this.credentialCreatedListener = credentialCreatedListener;
            return (Builder) super.setCredentialCreatedListener(credentialCreatedListener);
        }

        /**
         * Returns the credential created listener or {@code null} for none.
         */
        public final CredentialCreatedListener getGeneralCredentialCreatedListener() {
            return credentialCreatedListener;
        }

        @Override
        public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
            return (Builder) super.addRefreshListener(refreshListener);
        }

        @Override
        public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
            return (Builder) super.setRefreshListeners(refreshListeners);
        }
    }

    /**
     * Listener for a created credential after a successful token response in
     * {@link AuthorizationFlow#createAndStoreCredential(ImplicitResponseUrl, String)}
     * . .
     */
    public interface CredentialCreatedListener extends
            com.google.api.client.auth.oauth2.AuthorizationCodeFlow.CredentialCreatedListener {
        /**
         * Notifies of a created credential after a successful token response in
         * {@link AuthorizationFlow#createAndStoreCredential(ImplicitResponseUrl, String)}
         * .
         * <p>
         * Typical use is to parse additional fields from the credential
         * created, such as an ID token.
         * </p>
         *
         * @param credential       created credential
         * @param implicitResponse successful implicit response URL
         */
        void onCredentialCreated(Credential credential, ImplicitResponseUrl implicitResponse)
                throws IOException;
    }
}
