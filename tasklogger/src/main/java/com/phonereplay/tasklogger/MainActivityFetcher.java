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

            if (activities == null) {
                Log.d(TAG, "activities is null");
                return null;
            } else {
                Log.d(TAG, "activities size: " + activities.size());
                for (Map.Entry<Object, Object> entry : activities.entrySet()) {
                    Log.d(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
                }
            }

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
