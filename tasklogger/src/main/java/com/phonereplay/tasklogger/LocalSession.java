package com.phonereplay.tasklogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalSession {
    List<LocalActivity> activities;

    public LocalSession() {
        this.activities = new ArrayList<>();
    }

    public void addActivity(LocalActivity activity) {
        this.activities.add(activity);
    }

    public Optional<LocalActivity> getActivity(String activityId) {
        return this.activities.stream()
                .filter(activity -> activity.id.equals(activityId))
                .findFirst();
    }
}
