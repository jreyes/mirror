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
package com.vaporwarecorp.mirror.feature.spotify;

import android.content.Intent;
import android.media.session.MediaSession;
import android.support.annotation.NonNull;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.*;
import com.spotify.sdk.android.player.Player.InitializationObserver;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.feature.common.AbstractMirrorManager;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.Collections;
import java.util.List;

import static android.media.session.MediaSession.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.media.session.MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS;
import static solid.collectors.ToList.toList;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SpotifyManager.class)
public class SpotifyManagerImpl
        extends AbstractMirrorManager
        implements SpotifyManager, PlayerNotificationCallback, Callback<Track>, PlayerStateCallback {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    PluginFeatureManager mPluginFeatureManager;

    private String mClientId;
    private String mClientRedirect;
    private PlayerState mCurrentPlayerState;
    private Listener mListener;
    private Player mPlayer;
    private Config mPlayerConfig;
    private SpotifyService mService;
    private MediaSession mSession;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Callback ---------------------

    @Override
    public void success(Track track, Response response) {
        if (mListener != null && track != null) {
            mListener.onTrackUpdate(track);
        }
    }

    @Override
    public void failure(RetrofitError error) {
    }

// --------------------- Interface MirrorManager ---------------------

    @Override
    public void onFeaturePause() {
        Timber.d("onFeaturePause()");
        if (mSession != null && mSession.isActive()) {
            mSession.setActive(false);
        }
        Spotify.destroyPlayer(this);
    }

    @Override
    public void onFeatureResume() {
        Timber.d("onFeatureResume()");

        if (mService != null) {
            Timber.d("Service already running");
            return;
        }
    }

    @Override
    public void onFeatureStop() {
        Timber.d("onFeatureStop()");
        mPlayerConfig = null;
        mService = null;
    }

    @Override
    public void onViewStop() {
        Timber.d("onViewStop()");
        onFeaturePause();
    }

// --------------------- Interface PlayerNotificationCallback ---------------------

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        mCurrentPlayerState = playerState;

        Timber.d("onPlaybackEvent %s", eventType.toString());
        if (eventType == EventType.END_OF_CONTEXT) {
            if (mListener != null) {
                mListener.onPlaylistEnd();
            }
            return;
        }

        if (eventType == EventType.TRACK_CHANGED) {
            String trackId = playerState.trackUri.replaceAll("spotify:track:", "");
            mService.getTrack(trackId, this);
        }

        if (eventType == EventType.END_OF_CONTEXT) {
            onFeaturePause();
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
    }

// --------------------- Interface PlayerStateCallback ---------------------

    @Override
    public void onPlayerState(PlayerState playerState) {
        onPlaybackEvent(mCurrentPlayerState.playing ? EventType.PLAY : EventType.PAUSE, playerState);
    }

// --------------------- Interface SpotifyManager ---------------------

    @Override
    public void getNewReleases(NewReleasesCallback callback) {
        Observable.create((Observable.OnSubscribe<List<String>>) subscriber ->
                subscriber.onNext(stream(mService.getNewReleases().albums.items)
                        .flatMap(a -> mService.getAlbum(a.id).tracks.items)
                        .map(t -> t.uri)
                        .collect(toList())
                        .subList(0, 10)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(callback::onComplete, t -> callback.onComplete(Collections.emptyList()));
    }

    @Override
    public void pausePlaying() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public void play(@NonNull List<String> trackUris, @NonNull Listener listener) {
        mListener = listener;

        if (mService == null || trackUris.isEmpty()) {
            mListener.onPlaylistEnd();
            return;
        }

        final String trackIds = stream(trackUris)
                .map(t -> t.replaceAll("spotify:track:", ""))
                .reduce((t, u) -> t + "," + u)
                .get();
        mService.getTracks(trackIds, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                play(tracks);
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e(error, "Problem retrieving the tracks information");
            }
        });
    }

    @Override
    public void resumePlaying() {
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }

// --------------------- Interface WebAuthentication ---------------------

    @Override
    public void doAuthentication() {
        final AuthenticationRequest request = new AuthenticationRequest
                .Builder(mClientId, AuthenticationResponse.Type.TOKEN, mClientRedirect)
                .setScopes(new String[]{"streaming", "playlist-read-private", "user-library-read",})
                .build();
        AuthenticationClient.openLoginActivity(mPluginFeatureManager.getForegroundActivity(), REQUEST_CODE, request);
    }

    @Override
    public void isAuthenticated(IsAuthenticatedCallback callback) {
        mClientId = mAppManager.getString(R.string.spotify_client_id);
        mClientRedirect = mAppManager.getString(R.string.spotify_client_redirect);
        callback.onResult(false);
    }

    @Override
    public void onAuthenticationResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != SpotifyManager.REQUEST_CODE) {
            return;
        }

        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
        switch (response.getType()) {
            case TOKEN:
                onAuthenticationComplete(response.getAccessToken());
                break;
            case ERROR:
                Timber.e("Auth error: %s", response.getError());
                break;
            default:
                Timber.e("Auth result: %s", response.getType());
        }
    }

    private SpotifyService createSpotifyService(String accessToken) {
        return new RestAdapter.Builder()
                .setEndpoint(SpotifyApi.SPOTIFY_WEB_API_ENDPOINT)
                .setRequestInterceptor(r -> r.addHeader("Authorization", "Bearer " + accessToken))
                .build()
                .create(SpotifyService.class);
    }

    private void disable() {
        onFeaturePause();
    }

    private void onAuthenticationComplete(String accessToken) {
        mService = createSpotifyService(accessToken);
        if (mPlayer == null) {
            mPlayerConfig = new Config(mAppManager.getAppContext(), accessToken, mClientId);
        } else {
            mPlayer.login(accessToken);
        }
    }

    private void play(final Tracks tracks) {
        final List<String> trackUris = stream(tracks.tracks)
                .filter(track -> track != null && track.uri != null)
                .map(track -> track.uri)
                .collect(toList());
        if (trackUris.isEmpty()) {
            mListener.onPlaylistEnd();
            return;
        }

        mCurrentPlayerState = new PlayerState();

        Spotify.getPlayer(mPlayerConfig, this, new InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                // create a session so that a now playing card is active on the homescreen
                mSession = new MediaSession(mAppManager.getAppContext(), "SpotifySession");
                mSession.setFlags(FLAG_HANDLES_TRANSPORT_CONTROLS | FLAG_HANDLES_MEDIA_BUTTONS);

                mPlayer = player;
                mPlayer.addPlayerNotificationCallback(SpotifyManagerImpl.this);
                mPlayer.play(trackUris);
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Could not initialize player: %s", throwable.getMessage());
            }
        });
    }
}
