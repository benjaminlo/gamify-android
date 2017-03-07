package com.tribalhacks.gamify;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tribalhacks.gamify.spotify.SpotifyManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;

public class TrackRecyclerViewAdapter extends RecyclerView.Adapter {

    private List<Track> tracks = new ArrayList<>();

    private SpotifyManager spotifyManager;
    private OnTrackSelectedListener onTrackSelectedListener;

    TrackRecyclerViewAdapter(OnTrackSelectedListener onTrackSelectedListener, SpotifyManager spotifyManager) {
        super();
        this.onTrackSelectedListener = onTrackSelectedListener;
        this.spotifyManager = spotifyManager;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    public void setPlaylistTracks(List<PlaylistTrack> playlistTracks) {
        this.tracks.clear();
        for (PlaylistTrack playlistTrack : playlistTracks) {
            this.tracks.add(playlistTrack.track);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_track_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(this, spotifyManager, tracks.get(position));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_image)
        ImageView imageView;

        @BindView(R.id.song_name)
        TextView nameView;

        @BindView(R.id.artist)
        TextView artistView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void onBind(final TrackRecyclerViewAdapter trackRecyclerViewAdapter, final SpotifyManager spotifyManager, final Track track) {
            if (!track.album.images.isEmpty()) {
                Glide
                        .with(imageView.getContext())
                        .load(track.album.images.get(0).url)
                        .into(imageView);
            }

            nameView.setText(track.name);
            artistView.setText(track.artists.get(0).name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spotifyManager.onTrackSelected(track);
                    trackRecyclerViewAdapter.onTrackSelectedListener.onTrackSelected(track);
                }
            });
        }
    }
}
