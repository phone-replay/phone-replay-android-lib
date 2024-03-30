package com.phonereplay.tasklogger;

import android.os.Build;

import com.phonereplay.tasklogger.service.PhoneReplayService;

import java.util.Optional;

public class GestureRecorder {
    public final LocalSession currentSession;
    private final PhoneReplayService phoneReplayService;

    public GestureRecorder(String sessionId, String projectId, PhoneReplayService phoneReplayService) {
        this.phoneReplayService = phoneReplayService;
        this.currentSession = new LocalSession(sessionId, projectId);
    }

    public String generateSummaryLog() {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Session ID: ").append(currentSession.id).append("\n");
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

    public void sendLocalSessionData() {
        phoneReplayService.sendLocalSessionData(currentSession);
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
