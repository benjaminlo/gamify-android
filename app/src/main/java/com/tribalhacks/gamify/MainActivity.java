package com.tribalhacks.gamify;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter.Listener;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        socketManager = SocketManager.getInstance();

        socketManager.addListener(EVENT_ROOM_CREATED, onRoomCreated);
        socketManager.addListener(EVENT_BUTTON_CLICKED, onButtonClicked);
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
        super.onDestroy();
        socketManager.destroy();
    }
}
