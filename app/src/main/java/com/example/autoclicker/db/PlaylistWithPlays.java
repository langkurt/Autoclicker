package com.example.autoclicker.db;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PlaylistWithPlays {
    @Embedded
    public Playlist playList;

    @Relation(
            parentColumn = "playlistId",
            entityColumn = "playId"
    )
    public List<PlayItem> playItems;

    @NonNull
    @Override
    public String toString() {
        return String.format("Playlist: %s \t Plays: %s", playList, playItems);
    }
}
