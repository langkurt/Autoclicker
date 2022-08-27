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

    @Query("DELETE FROM playitem WHERE PlayItem.play_list_id = :playlistId")
    void deleteByPlaylistId(int playlistId);

    @Query("SELECT * FROM PlayItem WHERE PlayItem.play_list_id = :playlistId")
    List<PlayItem> getAllFromPlaylist(int playlistId);
}
