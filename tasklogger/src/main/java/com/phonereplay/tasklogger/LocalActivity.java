package com.phonereplay.tasklogger;


import java.util.HashSet;
import java.util.Set;

public class LocalActivity {
    String id;

    String activityName;

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

