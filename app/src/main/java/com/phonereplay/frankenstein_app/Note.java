package com.phonereplay.frankenstein_app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "my_notes")
public class Note {
    private String title;
    private String disp;

    @PrimaryKey(autoGenerate = true)
    private int id;

    public Note(String title, String disp) {
        this.title = title;
        this.disp = disp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisp() {
        return disp;
    }

    public void setDips(String disp) {
        this.disp = disp;
    }


}
