package com.example.autoclicker.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert
    void insert(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

    @Query("SELECT * FROM playlist")
    List<Playlist> getAll();

    @Transaction
    @Query("SELECT * FROM Playlist")
    public List<PlaylistWithPlays> getPlaylistsWithPlays();

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId = :playlistId")
    public PlaylistWithPlays getPlaylistWithPlays(int playlistId);
}
