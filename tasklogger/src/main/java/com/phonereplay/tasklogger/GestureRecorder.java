package com.phonereplay.tasklogger;

import android.os.Build;

import java.util.Optional;

public class GestureRecorder {
    public final LocalSession currentSession;

    public GestureRecorder() {
        this.currentSession = new LocalSession();
    }

    public String generateSummaryLog() {
        StringBuilder logBuilder = new StringBuilder();
        for (LocalActivity activity : currentSession.activities) {
            logBuilder.append("Activity ID: ").append(activity.id).append("\n");
            for (LocalGesture gesture : activity.gestures) {
                logBuilder.append("\tGesture: ").append(gesture.gestureType)
                        .append(" at Time: ").append(gesture.targetTime)
                        .append(", horário: ").append(gesture.createdAt).append("\n");
            }
        }
        return logBuilder.toString();
    }

    public void registerGesture(String activityName, String gestureType, String targetTime, String coordinates) {
        LocalActivity activity = findOrCreateActivity(activityName);
        assert activity != null;
        LocalGesture gesture = new LocalGesture(activity.id, gestureType, targetTime, coordinates);
        activity.addGesture(gesture);
    }

    private LocalActivity findOrCreateActivity(String activityName) {
        Optional<LocalActivity> existingActivity = currentSession.getActivity(activityName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (existingActivity.isPresent()) {
                return existingActivity.get();
            } else {
                LocalActivity newActivity = new LocalActivity(activityName);
                currentSession.addActivity(newActivity);
                return newActivity;
            }
        }
        return null;
    }
}
