package com.example.autoclicker.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Playlist.class, PlayItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaylistDao playlistDao();
    public abstract PlayDao playDao();
}
