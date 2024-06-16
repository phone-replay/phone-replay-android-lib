package com.phonereplay.tasklogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalSession {
    List<LocalActivity> activities;

    public LocalSession() {
        this.activities = new ArrayList<>();
    }

    public List<LocalActivity> getActivities() {
        return activities;
    }

    public void addActivity(LocalActivity activity) {
        this.activities.add(activity);
    }

    public Optional<LocalActivity> getActivity(String activityId) {
        return this.activities.stream()
                .filter(activity -> activity.getId().equals(activityId))
                .findFirst();
    }
}
