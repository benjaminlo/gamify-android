package com.tribalhacks.gamify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter.Listener;
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
import static com.tribalhacks.gamify.SocketManager.EVENT_USER_JOINED;
import static com.tribalhacks.gamify.SocketManager.KEY_IS_CORRECT;
import static com.tribalhacks.gamify.SocketManager.KEY_USER;
import static com.tribalhacks.gamify.SocketManager.KEY_USERNAME;

public class MainActivity extends AppCompatActivity {

    public static final String GAME_NAME_MUSIC = "Music Game";
    private static final String TAG = "GamifyMain";

    @BindView(R.id.room_id)
    TextView roomId;

    @BindView(R.id.player_response)
    TextView playerResponse;

    @BindView(R.id.button_play_pause)
    Button buttonPlayPause;

    private SocketManager socketManager;
    private SpotifyManager spotifyManager;

    private String username;

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

    private Listener onPlayerResponded = args -> {
        final JSONObject data = (JSONObject) args[0];
        runOnUiThread(() -> {
            try {
                username = data.getString(EVENT_USERNAME);
                playerResponse.setText(username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    };

    private Listener onUserJoined = args -> {
        final JSONObject data = (JSONObject) args[0];
        runOnUiThread(() -> {
            try {
                JSONObject user = data.getJSONObject(KEY_USER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        spotifyManager.createPlayer(this, requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        socketManager = SocketManager.getInstance();

        socketManager.addListener(EVENT_ROOM_CREATED, onRoomCreated);
        socketManager.addListener(EVENT_USER_JOINED, onUserJoined);
        socketManager.addListener(EVENT_BUTTON_CLICKED, onPlayerResponded);

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
            data.put(KEY_USERNAME, username);
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
            data.put(KEY_USERNAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketManager.emit(EVENT_CLEAR, data);
    }

    @OnClick(R.id.button_play_pause)
    void playPause() {
        if (spotifyManager.isPlaying()) {
            spotifyManager.pause();
            buttonPlayPause.setText("PLAY");
        } else {
            spotifyManager.resume();
            buttonPlayPause.setText("PAUSE");
        }
    }

    @OnClick(R.id.button_play_five_seconds)
    void playFiveSeconds() {
        spotifyManager.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 5000);
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_ten_seconds)
    void playTenSeconds() {
        spotifyManager.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 10000);
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_thirty_seconds)
    void playThirtySeconds() {
        spotifyManager.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V", 30000);
        buttonPlayPause.setText("PAUSE");
    }

    @Override
    protected void onDestroy() {
        socketManager.destroy();
        spotifyManager.destroyPlayer(this);
        super.onDestroy();
    }
}
