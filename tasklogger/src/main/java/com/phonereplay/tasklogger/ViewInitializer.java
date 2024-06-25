package com.phonereplay.tasklogger;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class ViewInitializer {

    private static View currentView;

    public static void initView(Activity activity) {
        String TAG = "initView";

        long startTime = System.nanoTime();

        Log.d(TAG, "Initializing view for activity: " + activity.getClass().getName());

        try {
            Class<?> flutterActivityClass = Class.forName("io.flutter.embedding.android.FlutterActivity");

            if (flutterActivityClass.isInstance(activity)) {
                ViewGroup rootView = activity.findViewById(android.R.id.content);
                if (rootView != null) {
                    Log.d(TAG, "Root view found with child count: " + rootView.getChildCount());
                    for (int i = 0; i < rootView.getChildCount(); i++) {
                        View child = rootView.getChildAt(i);
                        Log.d(TAG, "Child view at index " + i + ": " + child.getClass().getName());
                        if (child.getClass().getName().equals("io.flutter.embedding.android.FlutterView")) {
                            currentView = child;
                            Log.d(TAG, "FlutterView found and set as current view");
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Root view is null");
                }
            } else {
                currentView = activity.getWindow().getDecorView();
                Log.d(TAG, "DecorView set as current view");
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "FlutterActivity class not found", e);
            currentView = activity.getWindow().getDecorView();
            Log.d(TAG, "DecorView set as current view");
        }

        long endTime = System.nanoTime(); // Capture the end time
        long duration = endTime - startTime; // Calculate the duration

        Log.d(TAG, "initView execution time: " + duration + " nanoseconds");
    }

    public static void setupUserInteractionCallback(Activity activity) {
        Window window = activity.getWindow();

        if (window != null && !(window.getCallback() instanceof UserInteractionAwareCallback)) {
            window.setCallback(new UserInteractionAwareCallback(window.getCallback(), activity));
        }
        initView(activity);
    }

    public static View getCurrentView() {
        return currentView;
    }
}