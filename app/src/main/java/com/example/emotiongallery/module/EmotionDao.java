package com.example.emotiongallery.module;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EmotionDao {
    @Insert
    void insert(Emotion emotion);

    @Delete
    void delete(Emotion emotion);

    @Query("DELETE FROM Emotion WHERE sort = :sort")
    void deleteEmotionsBySort(String sort);

    @Query("SELECT * FROM Emotion")
    List<Emotion> getAllEmotions();

    @Query("SELECT * FROM Emotion WHERE sort = :sort")
    List<Emotion> getEmotionsBySort(String sort);
}
