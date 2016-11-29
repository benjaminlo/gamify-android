package com.tribalhacks.gamify.spotify;

import android.app.Activity;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.tribalhacks.gamify.utils.IntegerUtils;

public class SpotifyManager implements PlayerNotificationCallback, ConnectionStateCallback {

    public static final int REQUEST_CODE = IntegerUtils.getFreshInt();

    private static final String TAG = "GamifySpotify";
    private static final String REDIRECT_URI = "gamify://callback";
    private static SpotifyManager instance;
    private Player player;

    private SpotifyManager() {
        // no-op
    }

    public static SpotifyManager getInstance() {
        if (instance == null) {
            instance = new SpotifyManager();
        }

        return instance;
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
        player.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
