package com.phonereplay.tasklogger;

import android.os.Build;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LocalSession {
    String id;
    String projectId;
    Set<LocalActivity> activities;

    public LocalSession(String id, String projectId) {
        this.id = id;
        this.projectId = projectId;
        this.activities = new HashSet<>();
    }

    public void addActivity(LocalActivity activity) {
        this.activities.add(activity);
    }

    public Optional<LocalActivity> getActivity(String activityId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return this.activities.stream()
                    .filter(activity -> activity.id.equals(activityId))
                    .findFirst();
        }
        return null;
    }

    // Método para recuperar TimeLines de todas as atividades
    public Set<TimeLine> getTimeLines() {
        Set<TimeLine> timeLines = new HashSet<>();
        for (LocalActivity activity : activities) {
            for (LocalGesture gesture : activity.gestures) {
                TimeLine timeLine = new TimeLine(gesture.coordinates, gesture.gestureType, gesture.targetTime);
                timeLines.add(timeLine);
            }
        }
        return timeLines;
    }
}
