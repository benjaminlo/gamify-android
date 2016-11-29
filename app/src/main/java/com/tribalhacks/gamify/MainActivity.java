package com.tribalhacks.gamify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.tribalhacks.gamify.spotify.SpotifyKeys;
import com.tribalhacks.gamify.spotify.SpotifyManager;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tribalhacks.gamify.SocketManager.EVENT_BUTTON_CLICKED;
import static com.tribalhacks.gamify.SocketManager.EVENT_CLEAR;
import static com.tribalhacks.gamify.SocketManager.EVENT_CREATE_ROOM;
import static com.tribalhacks.gamify.SocketManager.EVENT_ROOM_CREATED;
import static com.tribalhacks.gamify.SocketManager.EVENT_ROOM_ID;
import static com.tribalhacks.gamify.SocketManager.EVENT_USERNAME;
import static com.tribalhacks.gamify.SocketManager.KEY_IS_CORRECT;

public class MainActivity extends AppCompatActivity {

    public static final String GAME_NAME_MUSIC = "Music Game";
    private static final String TAG = "GamifyMain";

    @BindView(R.id.room_id)
    TextView roomId;

    @BindView(R.id.player_response)
    TextView playerResponse;

    private SocketManager socketManager;
    private SpotifyManager spotifyManager;

    private Listener onRoomCreated = args -> {
        final JSONObject data = (JSONObject) args[0];
        runOnUiThread(() -> {
            try {
                roomId.setText(data.getString(EVENT_ROOM_ID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    };

    private Listener onButtonClicked = args -> {
        final JSONObject data = (JSONObject) args[0];
        runOnUiThread(() -> {
            try {
                playerResponse.setText(data.getString(EVENT_USERNAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SpotifyManager.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), SpotifyKeys.CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        player.addConnectionStateCallback(spotifyManager);
                        player.addPlayerNotificationCallback(spotifyManager);
                        spotifyManager.setPlayer(player);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        socketManager = SocketManager.getInstance();

        socketManager.addListener(EVENT_ROOM_CREATED, onRoomCreated);
        socketManager.addListener(EVENT_BUTTON_CLICKED, onButtonClicked);

        spotifyManager = SpotifyManager.getInstance();
        spotifyManager.authenticate(this);
    }

    @OnClick(R.id.button_create_room)
    void emitCreateRoom() {
        socketManager.emit(EVENT_CREATE_ROOM, GAME_NAME_MUSIC);
    }

    @OnClick(R.id.button_right)
    void emitRight() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_IS_CORRECT, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketManager.emit(EVENT_CLEAR, data);
    }

    @OnClick(R.id.button_wrong)
    void emitWrong() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_IS_CORRECT, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketManager.emit(EVENT_CLEAR, data);
    }

    @Override
    protected void onDestroy() {
        socketManager.destroy();
        Spotify.destroyPlayer(spotifyManager);
        super.onDestroy();
    }
}
