package com.tribalhacks.gamify;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.tribalhacks.gamify.spotify.SpotifyManager;
import com.tribalhacks.gamify.utils.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlaylistFragment extends Fragment {//implements TrackSelectedCallback {

    @BindView(R.id.edit_text_search)
    EditText editTextSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private SpotifyManager spotifyManager;
    //    private TrackRecyclerViewAdapter trackRecyclerViewAdapter;
    private PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        ButterKnife.bind(this, view);

        spotifyManager = SpotifyManager.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
//        trackRecyclerViewAdapter = new TrackRecyclerViewAdapter(this, spotifyManager);
//        recyclerView.setAdapter(trackRecyclerViewAdapter);
        playlistRecyclerViewAdapter = new PlaylistRecyclerViewAdapter(spotifyManager);
        recyclerView.setAdapter(playlistRecyclerViewAdapter);

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

        return view;
    }

    @OnClick(R.id.button_search)
    void onSearchButtonClicked(View view) {
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            String searchQuery = editTextSearch.getText().toString();
            if (!StringUtils.isEmptyOrNull(searchQuery)) {
//            spotifyManager.listSearch(this, searchQuery, trackRecyclerViewAdapter);
                recyclerView.smoothScrollToPosition(0);
            } else {
//            spotifyManager.getMyPlaylists(this, trackRecyclerViewAdapter);
                spotifyManager.getMyPlaylists(activity, playlistRecyclerViewAdapter);
            }
        }
    }

//    @Override
//    public void onTrackSelected(Track track) {
//        Glide
//                .with(playerImageView.getContext())
//                .load(track.album.images.get(0).url)
//                .into(playerImageView);
//
//        playerNameView.setText(track.name);
//        playerArtistVIew.setText(track.artists.get(0).name);
//
//        showPlayerControls();
//    }
}
