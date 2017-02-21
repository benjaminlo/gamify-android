package com.tribalhacks.gamify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter.Listener;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.tribalhacks.gamify.spotify.SpotifyManager;
import com.tribalhacks.gamify.utils.StringUtils;

import org.droidparts.widget.ClearableEditText;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kaaes.spotify.webapi.android.models.Track;

import static com.tribalhacks.gamify.SocketManager.EVENT_BUTTON_CLICKED;
import static com.tribalhacks.gamify.SocketManager.EVENT_CLEAR;
import static com.tribalhacks.gamify.SocketManager.EVENT_USERNAME;
import static com.tribalhacks.gamify.SocketManager.KEY_IS_CORRECT;
import static com.tribalhacks.gamify.SocketManager.KEY_USERNAME;

public class MainActivity extends AppCompatActivity implements PlayerNotificationCallback, TrackSelectedCallback {

    private static final String TAG = "GamifyMain";

    @BindView(R.id.room_id)
    TextView roomId;

    @BindView(R.id.player_response)
    TextView playerResponse;

    @BindView(R.id.button_play_pause)
    ImageButton buttonPlayPause;

    @BindView(R.id.edit_text_search)
    ClearableEditText editTextSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.response_buttons)
    LinearLayout responseButtonLayout;

    @BindView(R.id.game_controls)
    LinearLayout gameControls;

    @BindView(R.id.player_controls)
    LinearLayout playerControls;

    @BindView(R.id.player_album_image)
    ImageView playerImageView;

    @BindView(R.id.player_song_name)
    TextView playerNameView;

    @BindView(R.id.player_artist)
    TextView playerArtistVIew;

    private SocketManager socketManager;
    private SpotifyManager spotifyManager;
    private RecyclerViewAdapter recyclerViewAdapter;
    private String username;
    private Animation slideUpAnimation;
    private Animation slideDownAnimation;

    private Listener onPlayerResponded = new Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showGameControls();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        spotifyManager.createPlayer(this, this, requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        socketManager = SocketManager.getInstance();

        socketManager.addListener(EVENT_BUTTON_CLICKED, onPlayerResponded);

        spotifyManager = SpotifyManager.getInstance();
        spotifyManager.authenticate(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewAdapter = new RecyclerViewAdapter(this, spotifyManager);
        recyclerView.setAdapter(recyclerViewAdapter);

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

        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gameControls.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @OnClick(R.id.button_correct)
    void emitCorrect() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_IS_CORRECT, true);
            data.put(KEY_USERNAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketManager.emit(EVENT_CLEAR, data);
        hideGameControls();
    }

    @OnClick(R.id.button_incorrect)
    void emitIncorrect() {
        JSONObject data = new JSONObject();
        try {
            data.put(KEY_IS_CORRECT, false);
            data.put(KEY_USERNAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socketManager.emit(EVENT_CLEAR, data);
        hideGameControls();
    }

    @OnClick(R.id.button_play_pause)
    void playPause() {
        spotifyManager.playPause();
    }

    @OnClick(R.id.button_search)
    void onSearchButtonClicked(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        String searchQuery = editTextSearch.getText().toString();
        if (!StringUtils.isEmptyOrNull(searchQuery)) {
            spotifyManager.listSearch(this, searchQuery, recyclerViewAdapter);
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @OnClick(R.id.button_play_one_second)
    void playOneSecond() {
        spotifyManager.play(1000);
    }

    @OnClick(R.id.button_play_three_seconds)
    void playThreeSeconds() {
        spotifyManager.play(3000);
    }

    @OnClick(R.id.button_play_five_seconds)
    void playFiveSeconds() {
        spotifyManager.play(5000);
    }

    @OnClick(R.id.button_play_ten_seconds)
    void playTenSeconds() {
        spotifyManager.play(10000);
    }

    @Override
    protected void onDestroy() {
        socketManager.destroy();
        spotifyManager.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "onPlaybackEvent");
        switch (eventType) {
            case PLAY:
                buttonPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);
                break;
            case PAUSE:
                buttonPlayPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.d(TAG, "onPlaybackError");
    }

    @Override
    public void onTrackSelected(Track track) {
        Glide
                .with(playerImageView.getContext())
                .load(track.album.images.get(0).url)
                .into(playerImageView);

        playerNameView.setText(track.name);
        playerArtistVIew.setText(track.artists.get(0).name);

        showPlayerControls();
    }

    private void showPlayerControls() {
        if (playerControls.getVisibility() == View.GONE) {
            playerControls.setVisibility(View.VISIBLE);
            playerControls.startAnimation(slideUpAnimation);
        }
    }

    private void showGameControls() {
        if (gameControls.getVisibility() == View.GONE) {
            gameControls.setVisibility(View.VISIBLE);
            gameControls.startAnimation(slideUpAnimation);
        }
    }

    private void hideGameControls() {
        if (gameControls.getVisibility() == View.VISIBLE) {
            gameControls.startAnimation(slideDownAnimation);
        }
    }
}
