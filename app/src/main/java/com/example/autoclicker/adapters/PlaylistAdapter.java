package com.example.autoclicker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.autoclicker.R;
import com.example.autoclicker.db.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
    public static final String DEBUG_TAG = "PlaylistAdapter";

    private final List<Playlist> playlists;
    private final LayoutInflater playlistInflater;

    public PlaylistAdapter(Context context, List<Playlist> playlists){
        super(context, 0, playlists);
        this.playlists = playlists;
        this.playlistInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Playlist getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return playlists.get(position).playlistId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout playlistLayout = (LinearLayout)playlistInflater.inflate (R.layout.play_view, parent, false);

        // Get button references
        Button startButton = (Button)playlistLayout.findViewById(R.id.start);
        Button deleteButton = (Button)playlistLayout.findViewById(R.id.delete);

        // Get playlist using position
        Playlist currentPlaylist = playlists.get(position);

        // Get name and id of playlist.
        startButton.setText(currentPlaylist.name);
        startButton.setTag(currentPlaylist.playlistId);
        deleteButton.setTag(currentPlaylist.playlistId);

        // set position as tag
        playlistLayout.setTag(position);

        return playlistLayout;
    }
}
