package com.phonereplay.tasklogger;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Map;

public class MainActivityFetcher {
    private static final String TAG = "MainActivityFetcher";

    public static Activity getMainActivity() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class<?> activityRecordClass = activityRecord.getClass();
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);

                if (activity.getClass().getSimpleName().equals("MainActivity")) {
                    return activity;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get MainActivity via reflection", e);
        }
        return null;
    }
}
