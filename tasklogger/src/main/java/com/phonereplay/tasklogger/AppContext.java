package com.phonereplay.tasklogger;


import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

public class AppContext {
    private static Context context;

    @SuppressLint("PrivateApi")
    public static Context getContext() {
        if (context == null) {
            try {
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                @SuppressLint("DiscouragedPrivateApi") Method currentApplicationMethod =
                        activityThread.getDeclaredMethod("currentApplication");
                context = (Context) currentApplicationMethod.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to get application context via reflection", e);
            }
        }
        return context;
    }
}
