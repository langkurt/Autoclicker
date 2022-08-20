package com.example.autoclicker.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayDao {

    @Insert
    void insert(Play play);

    @Insert
    void insertAll(Play... plays);

    @Delete
    void delete(Play play);

    @Delete
    void deleteAll(Play... plays);

    @Query("SELECT * FROM play")
    List<Play> getAll();
}
