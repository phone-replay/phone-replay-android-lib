package com.phonereplay.tasklogger;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import com.phonereplay.tasklogger.reflect.Reflect;

public class PhoneReplay extends Activity {

    private static PhoneReplayApi phoneReplayApi;
    private static PhoneReplay sInstance;
    private static UserInteractionAwareCallback userInteractionAwareCallback;
    private final Context context;
    private Activity currentActivity;
    private int previousWidth;
    private int previousHeight;

    private PhoneReplay(String accessKey) {
        context = AppContext.getContext();
        phoneReplayApi = new PhoneReplayApi(context, accessKey);
    }

    public static void init(String accessKey) {
        PhoneReplay.getInstance(accessKey).attachBaseContext();
    }

    public synchronized static PhoneReplay getInstance(String accessKey) {
        if (sInstance == null) {
            sInstance = new PhoneReplay(accessKey);
        }
        return sInstance;
    }

    public static UserInteractionAwareCallback getUserInteractionAwareCallback() {
        return userInteractionAwareCallback;
    }

    private void updateDimensions(int width, int height) {
        if (previousWidth != 0) {
            phoneReplayApi.orientation = true;
        }
        previousWidth = width;
        previousHeight = height;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
        PhoneReplayApi.setCurrentActivity(activity);
    }

    private void replaceInstrumentation(Context contextImpl) {
        Reflect contextImplRef = Reflect.on(contextImpl);
        Reflect activityThreadRef = contextImplRef.field("mMainThread");
        Reflect instrumentationRef = activityThreadRef.field("mInstrumentation");
        TaskLoggerInstrumentation newInstrumentation = new TaskLoggerInstrumentation(instrumentationRef.get());
        activityThreadRef.set("mInstrumentation", newInstrumentation);
    }

    private void attachBaseContext() {
        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
        Context contextImpl = getContextImpl(context);
        replaceInstrumentation(contextImpl);
    }

    private Context getContextImpl(Context context) {
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) context).getBaseContext()) != null) {
            context = nextContext;
        }
        return context;
    }

    public void setupUserInteractionCallback(Activity activity) {
        Window window = activity.getWindow();
        if (window != null && !(window.getCallback() instanceof UserInteractionAwareCallback)) {
            userInteractionAwareCallback = new UserInteractionAwareCallback(window.getCallback(), activity);
            window.setCallback(userInteractionAwareCallback);
            Log.d("PhoneReplay", "UserInteractionAwareCallback set for activity: " + activity.getClass().getSimpleName());
        }
    }

    private class TaskLoggerInstrumentation extends Instrumentation {

        Instrumentation base;
        Reflect instrumentRef;

        public TaskLoggerInstrumentation(Instrumentation base) {
            this.base = base;
            instrumentRef = Reflect.on(base);
        }

        private void initThread(Activity activity) {
            if (!activity.equals(getCurrentActivity())) {
                phoneReplayApi.getmHandler().removeCallbacks(phoneReplayApi.getThread());
                phoneReplayApi.getmHandler().postDelayed(phoneReplayApi.getThread(), 100);
                setCurrentActivity(activity);
            }
            initView(activity);
        }

        @Override
        public void onCreate(Bundle arguments) {
            super.onCreate(arguments);
        }

        private void initView(Activity activity) {
            phoneReplayApi.setCurrentView(activity.getWindow().getDecorView());
            phoneReplayApi.getCurrentView().setDrawingCacheEnabled(true);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {
            initThread(activity);
            super.callActivityOnCreate(activity, bundle);
        }

        @Override
        public void callActivityOnNewIntent(Activity activity, Intent intent) {
            super.callActivityOnNewIntent(activity, intent);
        }

        @Override
        public void callActivityOnRestart(Activity activity) {
            super.callActivityOnRestart(activity);
        }

        @Override
        public void callActivityOnStart(Activity activity) {
            setupUserInteractionCallback(activity);
            initView(activity);
            super.callActivityOnStart(activity);
        }

        /**
         * Method to update and handle screen dimensions.
         *
         * @param activity The current Activity context to get display metrics.
         */
        public void updateAndHandleScreenDimensions(Activity activity) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            // Use activity's context to get the WindowManager
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            phoneReplayApi.setMainHeight(displayMetrics.heightPixels, displayMetrics.widthPixels);
            // Check if dimensions have changed
            if (displayMetrics.widthPixels != previousWidth || displayMetrics.heightPixels != previousHeight) {
                // Dimensions have changed, do something here
                updateDimensions(displayMetrics.widthPixels, displayMetrics.heightPixels); // Update stored dimensions
                // Add your logic to handle the resolution change
            }
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            updateAndHandleScreenDimensions(activity);
            super.callActivityOnResume(activity);
        }

        @Override
        public void callActivityOnPause(Activity activity) {
            super.callActivityOnPause(activity);
        }

        @Override
        public void callActivityOnStop(Activity activity) {
            super.callActivityOnStop(activity);
        }

        @Override
        public void callActivityOnDestroy(Activity activity) {
            super.callActivityOnDestroy(activity);
        }

        @Override
        public void callActivityOnUserLeaving(Activity activity) {
            super.callActivityOnUserLeaving(activity);
        }
    }
}
