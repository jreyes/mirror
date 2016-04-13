package com.vaporwarecorp.mirror.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.*;
import com.vaporwarecorp.mirror.event.SpotifyPlaybackEvent;
import com.vaporwarecorp.mirror.event.SpotifyTrackEvent;
import com.vaporwarecorp.mirror.event.VideoCompletedEvent;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import org.greenrobot.eventbus.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import java.util.List;
import java.util.Properties;

public class SpotifyManager implements PlayerNotificationCallback, Callback<Track>, PlayerStateCallback {
// ------------------------------ FIELDS ------------------------------

    public static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "SpotifyClientId";
    private static final String CLIENT_REDIRECT_URI = "SpotifyRedirectUri";

    private String mClientId;
    private String mClientRedirectUri;
    private Context mContext;
    private PlayerState mCurrentPlayerState;
    private Player mPlayer;
    private SpotifyService mService;

// --------------------------- CONSTRUCTORS ---------------------------

    public SpotifyManager(Context context, Properties properties) {
        mContext = context;
        mClientId = properties.getProperty(CLIENT_ID);
        mClientRedirectUri = properties.getProperty(CLIENT_REDIRECT_URI);
        mCurrentPlayerState = new PlayerState();
        mService = new SpotifyApi().getService();
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Callback ---------------------

    @Override
    public void success(Track track, Response response) {
        if (track != null) {
            mPlayer.getPlayerState(this);
            EventBus.getDefault().post(new SpotifyTrackEvent(track));
        }
    }

    @Override
    public void failure(RetrofitError error) {
    }

// --------------------- Interface PlayerNotificationCallback ---------------------

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        mCurrentPlayerState = playerState;

        int lastPosition = playerState.positionInMs;
        if (eventType == EventType.TRACK_CHANGED) {
            String trackId = playerState.trackUri.replaceAll("spotify:track:", "");
            mService.getTrack(trackId, this);
        }
        EventBus.getDefault().post(new SpotifyPlaybackEvent(lastPosition, eventType));
        if (eventType == EventType.END_OF_CONTEXT) {
            EventBus.getDefault().post(new VideoCompletedEvent());
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

// -------------------------- OTHER METHODS --------------------------

    public void authenticate(Activity activity) {
        AuthenticationRequest request = new AuthenticationRequest
                .Builder(mClientId, AuthenticationResponse.Type.TOKEN, mClientRedirectUri)
                .setScopes(new String[]{"user-read-private", "streaming"})
                .build();
        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }

    public void onDestroy() {
        Spotify.destroyPlayer(this);
    }

    public void play(List<String> trackUris) {
        if (trackUris == null || trackUris.isEmpty()) {
            return;
        }
        stop();
        mPlayer.play(trackUris);
    }

    public void processAuthentication(int resultCode, Intent data) {
        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
            Config playerConfig = new Config(mContext, response.getAccessToken(), mClientId);
            Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {
                    mPlayer = player;
                    mPlayer.addPlayerNotificationCallback(SpotifyManager.this);
                }

                @Override
                public void onError(Throwable throwable) {
                    Timber.e("Could not initialize player: %s", throwable.getMessage());
                }
            });
        }
    }

    public void stop() {
        if (mCurrentPlayerState.playing) {
            mPlayer.pause();
            mPlayer.clearQueue();
        }
    }

    public void togglePlay() {
        if (mCurrentPlayerState.playing) {
            mPlayer.pause();
        } else {
            mPlayer.resume();
        }
    }
}
