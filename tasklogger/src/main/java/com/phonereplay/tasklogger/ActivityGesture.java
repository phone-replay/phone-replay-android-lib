package com.phonereplay.tasklogger;


import java.util.ArrayList;
import java.util.List;

public class ActivityGesture {
    private final String activityName;
    private final List<Gesture> gestures;

    public ActivityGesture(String activityName) {
        this.activityName = activityName;
        this.gestures = new ArrayList<>();
    }

    public void addGesture(Gesture gesture) {
        gestures.add(gesture);
    }

    public String getActivityName() {
        return activityName;
    }

    public List<Gesture> getGestures() {
        return gestures;
    }
}
