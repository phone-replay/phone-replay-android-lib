package com.phonereplay.tasklogger;

import java.util.ArrayList;
import java.util.List;

public class Gesture {
    private final List<String[]> actions;

    public Gesture() {
        this.actions = new ArrayList<>();
    }

    public void addAction(String action, String targetTime, String gestureType) {
        actions.add(new String[]{action, targetTime, gestureType});
    }

    public List<String[]> getActions() {
        return actions;
    }
}