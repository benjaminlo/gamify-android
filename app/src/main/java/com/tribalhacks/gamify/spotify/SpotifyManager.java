package com.tribalhacks.gamify.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.tribalhacks.gamify.OnTrackSelectedListener;
import com.tribalhacks.gamify.PlaylistRecyclerViewAdapter;
import com.tribalhacks.gamify.SocketManager;
import com.tribalhacks.gamify.TrackRecyclerViewAdapter;
import com.tribalhacks.gamify.utils.IntegerUtils;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class SpotifyManager implements ConnectionStateCallback, OnTrackSelectedListener {

    public static final int REQUEST_CODE = IntegerUtils.getFreshInt();

    private static final String TAG = "GamifySpotify";
    private static final String REDIRECT_URI = "gamify://callback";
    private static SpotifyManager instance;
    private Player player;
    private SpotifyApi api = new SpotifyApi();
    private SpotifyService spotify;
    private Track selectedTrack;
    private boolean isNewSong;

    private SpotifyManager() {
        // no-op
    }

    public static SpotifyManager getInstance() {
        if (instance == null) {
            instance = new SpotifyManager();
        }

        return instance;
    }

    public void createPlayer(Context context, final PlayerNotificationCallback playerNotificationCallback,
                             int requestCode, int resultCode, Intent data) {
        if (requestCode == SpotifyManager.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                String token = response.getAccessToken();
                Config playerConfig = new Config(context, token, SpotifyKeys.CLIENT_ID);
                api.setAccessToken(token);
                spotify = api.getService();
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        player.addConnectionStateCallback(SpotifyManager.this);
                        player.addPlayerNotificationCallback(playerNotificationCallback);
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

        builder.setScopes(new String[]{"user-read-private", "streaming", // from Developer Guide
                "playlist-read-private", "playlist-read-collaborative"}); // access more playlists
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

    private boolean play() {
        if (selectedTrack != null) {
            player.play(selectedTrack.uri);
            return true;
        }

        return false;
    }

    public void play(int durationInMillis) {
        if (play()) {
            new CountDownTimer(durationInMillis, durationInMillis) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    player.pause();
                    SocketManager.getInstance().emit(SocketManager.EVENT_ANSWER_ALLOWED, true);
                }
            }.start();
        }
    }

    public void playPause() {
        player.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    player.pause();
                } else if (isNewSong) {
                    play();
                    isNewSong = false;
                } else {
                    player.resume();
                }
            }
        });
    }

    public void listSearch(final Activity activity, final String searchQuery, final TrackRecyclerViewAdapter adapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final TracksPager tracksPager = spotify.searchTracks(searchQuery);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setTracks(tracksPager.tracks.items);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onTrackSelected(Track track) {
        selectedTrack = track;
        isNewSong = true;
    }

    public void getMyPlaylists(final Activity activity, final PlaylistRecyclerViewAdapter adapter) {
        if (spotify != null) {
            spotify.getMyPlaylists(new SpotifyCallback<Pager<PlaylistSimple>>() {
                @Override
                public void failure(SpotifyError spotifyError) {

                }

                @Override
                public void success(final Pager<PlaylistSimple> playlistSimplePager, Response response) {
                    if (playlistSimplePager != null && playlistSimplePager.items != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setPlaylists(playlistSimplePager.items);
                            }
                        });
                    }
                }
            });
        }
    }

    public void getPlaylist(final Activity activity, PlaylistSimple playlist, final TrackRecyclerViewAdapter adapter) {
        String playlistId = playlist.id;
        String ownerId = playlist.owner.id;

        spotify.getPlaylist(ownerId, playlistId, new SpotifyCallback<Playlist>() {
            @Override
            public void failure(SpotifyError spotifyError) {

            }

            @Override
            public void success(final Playlist playlist, Response response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setPlaylistTracks(playlist.tracks.items);
                    }
                });
                Toast.makeText(activity, "Playlist Name: " + playlist.name, Toast.LENGTH_LONG).show();
            }
        });
    }
}
