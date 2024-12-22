package com.example.emotiongallery.module;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Emotion.class}, version = 1)
public abstract class EmotionDB extends RoomDatabase {
    public abstract EmotionDao emotionDao();
}
