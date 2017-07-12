package com.tribalhacks.gamify;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

public class PlaylistFragment extends Fragment {

    @BindView(R.id.edit_text_search)
    EditText editTextSearch;

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

    @Override
    public void onStart() {
        super.onStart();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            editTextSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextSearch, 0);
        }
    }

    @OnClick(R.id.button_search)
    void onSearchButtonClicked(View view) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            String searchQuery = editTextSearch.getText().toString();
            if (!StringUtils.isEmptyOrNull(searchQuery)) {
                playlistRecyclerView.smoothScrollToPosition(0);
            } else {
                spotifyManager.getMyPlaylists(activity, playlistRecyclerViewAdapter);
            }
        }
    }
}
