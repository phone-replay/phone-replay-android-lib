package com.phonereplay.tasklogger;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class MainActivityFetcher {
    private static final String TAG = "MainActivityFetcher";

    public static Activity getOnlyMainActivity() {
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

                    Object activityRecord = entry.getValue();
                    Class<?> activityRecordClass = activityRecord.getClass();
                    for (Field field : activityRecordClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(activityRecord);
                        Log.d(TAG, "Field: " + field.getName() + ", Value: " + fieldValue);
                    }
                }
            }
            for (Object activityRecord : activities.values()) {
                Class<?> activityRecordClass = activityRecord.getClass();
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);

                Log.d(TAG, "Activity class: " + activity.getClass().getName());

                if (activity.getClass().getSimpleName().equals("MainActivity")) {
                    logFlutterActivityDetails(activity);
                    return activity;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get MainActivity via reflection", e);
        }
        return null;
    }

    private static void logFlutterActivityDetails(Activity activity) {
        try {
            Class<?> flutterActivityClass = activity.getClass();

            for (Field field : flutterActivityClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(activity);
                Log.d(TAG, "FlutterActivity Field: " + field.getName() + ", Value: " + fieldValue);
            }

            for (Method method : flutterActivityClass.getDeclaredMethods()) {
                method.setAccessible(true);
                Log.d(TAG, "FlutterActivity Method: " + method.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to log FlutterActivity details", e);
        }
    }
}
