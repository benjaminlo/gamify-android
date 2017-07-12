package com.tribalhacks.gamify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tribalhacks.gamify.spotify.SpotifyManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistFragment extends Fragment {

    @BindView(R.id.playlist_recycler_view)
    RecyclerView playlistRecyclerView;

    private SpotifyManager spotifyManager;
    private PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        ButterKnife.bind(this, view);

        spotifyManager = SpotifyManager.getInstance();

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            playlistRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            playlistRecyclerViewAdapter = new PlaylistRecyclerViewAdapter(activity, spotifyManager);
            playlistRecyclerView.setAdapter(playlistRecyclerViewAdapter);
        }

        return view;
    }
}
