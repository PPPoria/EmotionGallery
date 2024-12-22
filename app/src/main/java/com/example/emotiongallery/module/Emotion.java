package com.example.emotiongallery.module;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Emotion")
public class Emotion {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "sort")
    public String sort;
    @ColumnInfo(name = "file_name")
    public String fileName;

    public Emotion(int id, String sort, String fileName) {
        this.id = id;
        this.sort = sort;
        this.fileName = fileName;
    }

    public Emotion() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
