package com.phonereplay.tasklogger;


import java.util.HashSet;
import java.util.Set;

public class LocalActivity {
    private final String id;
    private final String activityName;
    private final Set<LocalGesture> gestures;

    public LocalActivity(String id) {
        this.id = id;
        this.activityName = id;
        this.gestures = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getActivityName() {
        return activityName;
    }

    public Set<LocalGesture> getGestures() {
        return gestures;
    }

    public void addGesture(LocalGesture gesture) {
        this.gestures.add(gesture);
    }
}

