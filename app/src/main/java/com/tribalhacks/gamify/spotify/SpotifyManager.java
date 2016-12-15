package com.tribalhacks.gamify.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.tribalhacks.gamify.utils.IntegerUtils;

public class SpotifyManager implements PlayerNotificationCallback, ConnectionStateCallback {

    public static final int REQUEST_CODE = IntegerUtils.getFreshInt();

    private static final String TAG = "GamifySpotify";
    private static final String REDIRECT_URI = "gamify://callback";
    private static SpotifyManager instance;
    private Player player;
    private boolean isPlaying = false;

    private SpotifyManager() {
        // no-op
    }

    public static SpotifyManager getInstance() {
        if (instance == null) {
            instance = new SpotifyManager();
        }

        return instance;
    }

    public void createPlayer(Context context, int requestCode, int resultCode, Intent data) {
        if (requestCode == SpotifyManager.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(context, response.getAccessToken(), SpotifyKeys.CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        player.addConnectionStateCallback(SpotifyManager.this);
                        player.addPlayerNotificationCallback(SpotifyManager.this);
                        SpotifyManager.this.player = player;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    public void destroyPlayer(Activity activity) {
        Spotify.destroyPlayer(activity);
    }

    public void authenticate(Activity activity) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(SpotifyKeys.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "onLoggedIn");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "onLoggedOut");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d(TAG, "onLoginFailed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "onTemporaryError");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage");
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "onPlaybackEvent");
        switch (eventType) {
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.d(TAG, "onPlaybackError");
        switch (errorType) {
            default:
                break;
        }
    }

    public void play(String uri) {
        player.play(uri);
        isPlaying = true;
    }

    public void play(String uri, int durationInMillis) {
        play(uri);
        new CountDownTimer(durationInMillis, durationInMillis) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                pause();
            }
        }.start();
    }

    public void pause() {
        player.pause();
        isPlaying = false;
    }

    public void resume() {
        player.resume();
        isPlaying = true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
