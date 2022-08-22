package com.example.autoclicker.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PlayItem {

    @PrimaryKey(autoGenerate = true)
    public int playId;

    @ColumnInfo(name = "play_list_id")
    public long playListId;

    @ColumnInfo(name = "x_coordinate")
    public float xCoordinate;

    @ColumnInfo(name = "y_coordinate")
    public float yCoordinate;

    @ColumnInfo(name = "delay")
    public float delay;
}
