package com.tribalhacks.gamify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.tribalhacks.gamify.spotify.SpotifyManager;
import com.tribalhacks.gamify.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tribalhacks.gamify.SocketManager.EVENT_ANSWER_ALLOWED;
import static com.tribalhacks.gamify.SocketManager.EVENT_BUTTON_CLICKED;
import static com.tribalhacks.gamify.SocketManager.EVENT_CLEAR;
import static com.tribalhacks.gamify.SocketManager.EVENT_ROOM_CREATED;
import static com.tribalhacks.gamify.SocketManager.EVENT_ROOM_ID;
import static com.tribalhacks.gamify.SocketManager.EVENT_USERNAME;
import static com.tribalhacks.gamify.SocketManager.EVENT_USER_JOINED;
import static com.tribalhacks.gamify.SocketManager.KEY_IS_CORRECT;
import static com.tribalhacks.gamify.SocketManager.KEY_USER;
import static com.tribalhacks.gamify.SocketManager.KEY_USERNAME;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GamifyMain";

    @BindView(R.id.room_id)
    TextView roomId;

    @BindView(R.id.player_response)
    TextView playerResponse;

    @BindView(R.id.button_play_pause)
    Button buttonPlayPause;

    @BindView(R.id.edit_text_search)
    EditText editTextSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private SocketManager socketManager;
    private SpotifyManager spotifyManager;
    private RecyclerViewAdapter adapter;

    private String username;

    private Listener onRoomCreated = new Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        roomId.setText(data.getString(EVENT_ROOM_ID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Listener onPlayerResponded = new Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        username = data.getString(EVENT_USERNAME);
                        playerResponse.setText(username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Listener onUserJoined = new Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject user = data.getJSONObject(KEY_USER);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new RecyclerViewAdapter(spotifyManager);
        recyclerView.setAdapter(adapter);

        editTextSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onSearchButtonClicked(view);
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.button_answer_allowed)
    void emitCreateRoom() {
        socketManager.emit(EVENT_ANSWER_ALLOWED, true);
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

    @OnClick(R.id.button_search)
    void onSearchButtonClicked(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        String searchQuery = editTextSearch.getText().toString();
        if (!StringUtils.isEmptyOrNull(searchQuery)) {
            spotifyManager.listSearch(this, searchQuery, adapter);
        }
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_one_second)
    void playOneSecond() {
        spotifyManager.play(1000);
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_three_seconds)
    void playThreeSeconds() {
        spotifyManager.play(3000);
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_five_seconds)
    void playFiveSeconds() {
        spotifyManager.play(5000);
        buttonPlayPause.setText("PAUSE");
    }

    @OnClick(R.id.button_play_ten_seconds)
    void playTenSeconds() {
        spotifyManager.play(10000);
        buttonPlayPause.setText("PAUSE");
    }

    @Override
    protected void onDestroy() {
        socketManager.destroy();
        spotifyManager.destroyPlayer(this);
        super.onDestroy();
    }
}
