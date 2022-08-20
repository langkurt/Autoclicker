package com.example.autoclicker.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    public int playlistId;

    @ColumnInfo(name = "name")
    public String name;
}
