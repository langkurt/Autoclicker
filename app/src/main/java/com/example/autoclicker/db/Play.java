package com.example.autoclicker.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Play {

    @PrimaryKey(autoGenerate = true)
    public int playId;

    @ColumnInfo(name = "play_list_id")
    public long PlayListId;

    @ColumnInfo(name = "x_coordinate")
    public long xCoordinate;

    @ColumnInfo(name = "y_coordinate")
    public long yCoordinate;

    @ColumnInfo(name = "delay")
    public long delay;
}
