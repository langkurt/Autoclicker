package com.example.autoclicker.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayDao {

    @Insert
    void insert(PlayItem playItem);

    @Insert
    void insertAll(PlayItem... playItems);

    @Delete
    void delete(PlayItem playItem);

    @Delete
    void deleteAll(PlayItem... playItems);

    @Query("SELECT * FROM PlayItem")
    List<PlayItem> getAll();

    @Query("SELECT * FROM PlayItem WHERE PlayItem.play_list_id = :playlistId")
    List<PlayItem> getAllFromPlaylist(int playlistId);
}
