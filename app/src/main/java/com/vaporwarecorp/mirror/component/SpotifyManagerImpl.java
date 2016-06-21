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
package com.vaporwarecorp.mirror.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import com.robopupu.api.component.AbstractManager;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.robopupu.api.plugin.PluginBus;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.*;
import com.spotify.sdk.android.player.Player.InitializationObserver;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.List;

import static android.media.session.MediaSession.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.media.session.MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS;
import static solid.collectors.ToList.toList;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SpotifyManager.class)
public class SpotifyManagerImpl
        extends AbstractManager
        implements SpotifyManager, PlayerNotificationCallback, Callback<Track>, PlayerStateCallback {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;

    private String mClientId;
    private String mClientRedirectUri;
    private Context mContext;
    private PlayerState mCurrentPlayerState;
    private List<Track> mCurrentTracks;
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
            stop();
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

// --------------------- Interface PluginComponent ---------------------

    @Override
    public void onPlugged(PluginBus bus) {
        mContext = mAppManager.getAppContext();
        mClientId = mAppManager.getApplicationProperties().getProperty(CLIENT_ID);
        mClientRedirectUri = mAppManager.getApplicationProperties().getProperty(CLIENT_REDIRECT_URI);
        mService = new SpotifyApi().getService();
    }

// --------------------- Interface SpotifyManager ---------------------

    @Override
    public void authenticate(Activity activity) {
        AuthenticationRequest request = new AuthenticationRequest
                .Builder(mClientId, AuthenticationResponse.Type.TOKEN, mClientRedirectUri)
                .setScopes(new String[]{"user-read-private", "streaming"})
                .build();
        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }

    @Override
    public void play(List<String> trackUris, Listener listener) {
        if (trackUris == null || trackUris.isEmpty()) {
            return;
        }

        mListener = listener;

        String trackIds = stream(trackUris)
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
    public void processAuthentication(int resultCode, Intent data) {
        Observable
                .just(AuthenticationClient.getResponse(resultCode, data))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AuthenticationResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(AuthenticationResponse response) {
                        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                            mPlayerConfig = new Config(mContext, response.getAccessToken(), mClientId);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        if (mSession != null && mSession.isActive()) {
            mSession.setActive(false);
        }
        Spotify.destroyPlayer(this);
    }

    private void play(Tracks tracks) {
        mCurrentPlayerState = new PlayerState();
        mCurrentTracks = tracks.tracks;

        Spotify.getPlayer(mPlayerConfig, this, new InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                // create a session so that a now playing card is active on the homescreen
                mSession = new MediaSession(mContext, "SpotifySession");
                mSession.setFlags(FLAG_HANDLES_TRANSPORT_CONTROLS | FLAG_HANDLES_MEDIA_BUTTONS);

                mPlayer = player;
                mPlayer.addPlayerNotificationCallback(SpotifyManagerImpl.this);
                mPlayer.play(stream(mCurrentTracks).map(t -> t.uri).collect(toList()));
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Could not initialize player: %s", throwable.getMessage());
            }
        });
    }
}
