package com.tribalhacks.gamify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.tribalhacks.gamify.spotify.SpotifyManager;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tribalhacks.gamify.SocketManager.EVENT_BUTTON_CLICKED;
import static com.tribalhacks.gamify.SocketManager.EVENT_CLEAR;
import static com.tribalhacks.gamify.SocketManager.EVENT_USERNAME;
import static com.tribalhacks.gamify.SocketManager.KEY_IS_CORRECT;
import static com.tribalhacks.gamify.SocketManager.KEY_USERNAME;

public class MainActivity extends AppCompatActivity implements PlayerNotificationCallback {

    private static final String TAG = "GamifyMain";

    @BindView(R.id.room_id)
    TextView roomId;

    @BindView(R.id.player_response)
    TextView playerResponse;

    @BindView(R.id.button_play_pause)
    ImageButton buttonPlayPause;

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

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new PlaylistFragment())
                .commit();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, new TrackFragment())
//                .commit();
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
