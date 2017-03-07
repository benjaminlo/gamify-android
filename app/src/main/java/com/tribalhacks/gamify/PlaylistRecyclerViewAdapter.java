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
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter {

    private List<PlaylistSimple> playlists = new ArrayList<>();

    private SpotifyManager spotifyManager;
//    private PlaylistSelectedCallback playlistSelectedCallback;

    PlaylistRecyclerViewAdapter(SpotifyManager spotifyManager) {
        super();
//        this.playlistSelectedCallback = playlistSelectedCallback;
        this.spotifyManager = spotifyManager;
    }

    public void setPlaylists(List<PlaylistSimple> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_playlist_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(this, spotifyManager, playlists.get(position));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.playlist_image)
        ImageView imageView;

        @BindView(R.id.playlist_name)
        TextView nameView;

        @BindView(R.id.owner_id)
        TextView ownerView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void onBind(final PlaylistRecyclerViewAdapter playlistRecyclerViewAdapter, final SpotifyManager spotifyManager, final PlaylistSimple playlist) {
            Glide
                    .with(imageView.getContext())
                    .load(playlist.images.get(0).url)
                    .into(imageView);

            nameView.setText(playlist.name);
            ownerView.setText(playlist.owner.id);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    spotifyManager.getMyPlaylists();
//                    spotifyManager.onTrackSelected(playlist);
//                    playlistRecyclerViewAdapter.PlaylistSelectedCallback.onPlaylistSelected(playlist);
                }
            });
        }
    }
}
