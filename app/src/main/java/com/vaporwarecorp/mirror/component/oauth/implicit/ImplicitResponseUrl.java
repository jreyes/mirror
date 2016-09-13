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

package com.vaporwarecorp.mirror.component.oauth.implicit;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.UrlEncodedParser;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.escape.CharEscapers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ImplicitResponseUrl extends GenericUrl {
// ------------------------------ FIELDS ------------------------------

    /**
     * Access token issued by the authorization server.
     */
    @Key("access_token")
    private String accessToken;

    /**
     * Error code ({@code "invalid_request"}, {@code "unauthorized_client"},
     * {@code "access_denied"}, {@code "unsupported_response_type"},
     * {@code "invalid_scope"}, {@code "server_error"},
     * {@code "temporarily_unavailable"}, or an extension error code as
     * specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional
     * Error Codes</a>) or {@code null} for none.
     */
    @Key
    private String error;

    /**
     * Human-readable text providing additional information used to assist the
     * client developer in understanding the error that occurred or {@code null}
     * for none.
     */
    @Key("error_description")
    private String errorDescription;

    /**
     * URI identifying a human-readable web page with information about the
     * error used to provide the client developer with additional information
     * about the error or {@code null} for none.
     */
    @Key("error_uri")
    private String errorUri;

    /**
     * Lifetime in seconds of the access token (for example 3600 for an hour) or
     * {@code null} for none.
     */
    @Key("expires_in")
    private Long expiresInSeconds;

    /**
     * Scope of the access token as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token
     * Scope</a> or {@code null} for none.
     */
    @Key
    private String scope;

    /**
     * State parameter matching the state parameter in the authorization request
     * or {@code null} for none.
     */
    @Key
    private String state;

    /**
     * Token type (as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token
     * Types</a>).
     */
    @Key("token_type")
    private String tokenType;

// -------------------------- STATIC METHODS --------------------------

    /**
     * Returns the URI for the given encoded URL.
     * <p>
     * Any {@link URISyntaxException} is wrapped in an
     * {@link IllegalArgumentException}.
     * </p>
     *
     * @param encodedUrl encoded URL
     * @return URI
     */
    private static URI toURI(String encodedUrl) {
        try {
            return new URI(encodedUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    ImplicitResponseUrl() {
        super();
    }

    public ImplicitResponseUrl(String encodedUrl) {
        this(toURI(encodedUrl));
    }

    ImplicitResponseUrl(URI uri) {
        this(uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getRawPath(),
                uri.getRawFragment(),
                uri.getRawQuery(),
                uri.getRawUserInfo());
    }

    ImplicitResponseUrl(URL url) {
        this(url.getProtocol(),
                url.getHost(),
                url.getPort(),
                url.getPath(),
                url.getRef(),
                url.getQuery(),
                url.getUserInfo());
    }

    private ImplicitResponseUrl(String scheme, String host, int port, String path, String fragment,
                                String query, String userInfo) {
        setScheme(scheme);
        setHost(host);
        setPort(port);
        setPathParts(toPathParts(path));
        setFragment(fragment != null ? CharEscapers.decodeUri(fragment) : null);
        if (fragment != null) {
            UrlEncodedParser.parse(fragment, this);
        }
        // no need for query parameters
        setUserInfo(userInfo != null ? CharEscapers.decodeUri(userInfo) : null);
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Returns the access token issued by the authorization server.
     */
    public final String getAccessToken() {
        return accessToken;
    }

    /**
     * Returns the error code ({@code "invalid_request"},
     * {@code "unauthorized_client"}, {@code "access_denied"},
     * {@code "unsupported_response_type"}, {@code "invalid_scope"},
     * {@code "server_error"}, {@code "temporarily_unavailable"}, or an
     * extension error code as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional
     * Error Codes</a>) or {@code null} for none.
     */
    public final String getError() {
        return error;
    }

    /**
     * Returns the human-readable text providing additional information used to
     * assist the client developer in understanding the error that occurred or
     * {@code null} for none.
     */
    public final String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Returns the URI identifying a human-readable web page with information
     * about the error used to provide the client developer with additional
     * information about the error or {@code null} for none.
     */
    public final String getErrorUri() {
        return errorUri;
    }

    /**
     * Returns the lifetime in seconds of the access token (for example 3600 for
     * an hour) or {@code null} for none.
     */
    public final Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    /**
     * Returns the scope of the access token or {@code null} for none.
     */
    public final String getScope() {
        return scope;
    }

    /**
     * Returns the state parameter matching the state parameter in the
     * authorization request or {@code null} for none.
     */
    public final String getState() {
        return state;
    }

    /**
     * Returns the token type (as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token
     * Types</a>).
     */
    public final String getTokenType() {
        return tokenType;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public ImplicitResponseUrl clone() {
        return (ImplicitResponseUrl) super.clone();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public ImplicitResponseUrl set(String fieldName, Object value) {
        return (ImplicitResponseUrl) super.set(fieldName, value);
    }

    /**
     * Sets the access token issued by the authorization server.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setAccessToken(String accessToken) {
        this.accessToken = Preconditions.checkNotNull(accessToken);
        return this;
    }

    /**
     * Sets the error code ({@code "invalid_request"},
     * {@code "unauthorized_client"}, {@code "access_denied"},
     * {@code "unsupported_response_type"}, {@code "invalid_scope"},
     * {@code "server_error"}, {@code "temporarily_unavailable"}, or an
     * extension error code as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional
     * Error Codes</a>) or {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setError(String error) {
        this.error = error;
        return this;
    }

    /**
     * Sets the human-readable text providing additional information used to
     * assist the client developer in understanding the error that occurred or
     * {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }

    /**
     * Sets the URI identifying a human-readable web page with information about
     * the error used to provide the client developer with additional
     * information about the error or {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setErrorUri(String errorUri) {
        this.errorUri = errorUri;
        return this;
    }

    /**
     * Sets the lifetime in seconds of the access token (for example 3600 for an
     * hour) or {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
        return this;
    }

    /**
     * Sets the scope of the access token or {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setScope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Sets the state parameter matching the state parameter in the
     * authorization request or {@code null} for none.
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the token type (as specified in <a
     * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token
     * Types</a>).
     * <p>
     * Overriding is only supported for the purpose of calling the super
     * implementation and changing the return type, but nothing else.
     * </p>
     */
    public ImplicitResponseUrl setTokenType(String tokenType) {
        this.tokenType = Preconditions.checkNotNull(tokenType);
        return this;
    }
}
