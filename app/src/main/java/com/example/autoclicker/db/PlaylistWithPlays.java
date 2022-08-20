package com.example.autoclicker.db;

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
    public List<Play> plays;
}
