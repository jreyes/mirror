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
import android.support.annotation.NonNull;
import com.robopupu.api.dependency.Provides;
import com.robopupu.api.dependency.Scope;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.*;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player.NotificationCallback;
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
import solid.functions.Action1;
import timber.log.Timber;

import java.util.Collections;
import java.util.List;

import static solid.collectors.ToList.toList;
import static solid.stream.Stream.stream;

@Plugin
@Scope(MirrorAppScope.class)
@Provides(SpotifyManager.class)
public class SpotifyManagerImpl
        extends AbstractMirrorManager
        implements SpotifyManager, NotificationCallback, Callback<Track> {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug
    PluginFeatureManager mPluginFeatureManager;

    private String mClientId;
    private String mClientRedirect;
    private String mCurrentUri;
    private Listener mListener;
    private Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Timber.d("OK!");
        }

        @Override
        public void onError(Error error) {
            Timber.e("ERROR:" + error);
        }
    };
    private SpotifyPlayer mPlayer;
    private Config mPlayerConfig;
    private SpotifyService mService;

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
        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(this);
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

// --------------------- Interface NotificationCallback ---------------------

    @Override
    public void onPlaybackEvent(PlayerEvent eventType) {
        Timber.d("onPlaybackEvent %s", eventType.toString());
        if (eventType == PlayerEvent.kSpPlaybackNotifyAudioDeliveryDone) {
            if (mListener != null) {
                mListener.onPlaylistEnd();
            }
            return;
        }

        if (eventType == PlayerEvent.kSpPlaybackNotifyTrackChanged) {
            mCurrentUri = mPlayer.getMetadata().currentTrack.uri;
            mService.getTrack(mCurrentUri.replaceAll("spotify:track:", ""), this);
        }
    }

    @Override
    public void onPlaybackError(Error error) {
    }

// --------------------- Interface SpotifyManager ---------------------

    @Override
    public void getNewReleases(NewReleasesCallback callback) {
        if (mService == null) {
            callback.onComplete(Collections.emptyList());
            return;
        }

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
            mPlayer.pause(mOperationCallback);
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
            mPlayer.resume(mOperationCallback);
        }
    }

    @Override
    public String getCurrentUri() {
        return mCurrentUri;
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
            mPlayer = Spotify.getPlayer(mPlayerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    mPlayer.addNotificationCallback(SpotifyManagerImpl.this);
                    //
                }

                @Override
                public void onError(Throwable t) {
                    Timber.e(t, "Could not initialize player: %s", t.getMessage());
                }
            });
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

        Timber.d("trackUris %s", trackUris);
        stream(trackUris).skip(1).forEach((Action1<String>) u -> mPlayer.queue(mOperationCallback, u));
        mCurrentUri = trackUris.get(0);
        mPlayer.playUri(mOperationCallback, mCurrentUri, 0, 0);
    }
}
