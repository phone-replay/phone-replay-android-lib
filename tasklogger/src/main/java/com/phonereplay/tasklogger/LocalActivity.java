package com.phonereplay.tasklogger;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public class LocalActivity {
    @SerializedName("id")
    String id;

    @SerializedName("activityName")
    String activityName;

    @SerializedName("gestures")
    Set<LocalGesture> gestures;

    public LocalActivity(String id) {
        this.id = id;
        this.activityName = id;
        this.gestures = new HashSet<>();
    }

    public void addGesture(LocalGesture gesture) {
        this.gestures.add(gesture);
    }
}

