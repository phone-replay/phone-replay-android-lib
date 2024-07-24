package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class AppContext {
    private static final String TAG = "AppContextConfig";
    private static Context context;

    @SuppressLint("PrivateApi")
    public static Context getContext() {
        if (context == null) {
            try {
                Log.d(TAG, "Attempting to get application context via reflection.");
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                @SuppressLint("DiscouragedPrivateApi") Method currentApplicationMethod =
                        activityThread.getDeclaredMethod("currentApplication");
                context = (Context) currentApplicationMethod.invoke(null);
                Log.d(TAG, "Application context acquired successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to get application context via reflection", e);
                throw new IllegalStateException("Failed to get application context via reflection", e);
            }
        } else {
            Log.d(TAG, "Application context already available.");
        }
        return context;
    }
}
