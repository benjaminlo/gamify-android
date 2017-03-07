package com.tribalhacks.gamify;

import android.content.Context;
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
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class TrackFragment extends Fragment {

    @BindView(R.id.tracks_recycler_view)
    RecyclerView tracksRecyclerView;

    private SpotifyManager spotifyManager;
    private TrackRecyclerViewAdapter trackRecyclerViewAdapter;
    private OnTrackSelectedListener trackSelectedListener;
    private PlaylistSimple playlist;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            trackSelectedListener = (OnTrackSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracks, container, false);

        ButterKnife.bind(this, view);

        spotifyManager = SpotifyManager.getInstance();

        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            tracksRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            trackRecyclerViewAdapter = new TrackRecyclerViewAdapter(trackSelectedListener, spotifyManager);
            tracksRecyclerView.setAdapter(trackRecyclerViewAdapter);

            if (playlist != null) {
                spotifyManager.getPlaylist(activity, playlist, trackRecyclerViewAdapter);
            }
        }

        return view;
    }

    public void setPlaylist(PlaylistSimple playlist) {
        this.playlist = playlist;
    }
}
