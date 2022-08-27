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
    long insert(Playlist playlist);

    @Query("DELETE FROM playlist WHERE playlistId = :playlistId")
    void deleteById(int playlistId);

    @Query("SELECT * FROM playlist")
    List<Playlist> getAll();

    @Query("SELECT * FROM playlist WHERE playlistId = :playlistId")
    Playlist getOne(int playlistId);

    @Transaction
    @Query("SELECT * FROM Playlist")
    public List<PlaylistWithPlays> getPlaylistsWithPlays();

    @Transaction
    @Query("SELECT * FROM Playlist WHERE playlistId = :playlistId")
    public PlaylistWithPlays getPlaylistWithPlays(int playlistId);
}
