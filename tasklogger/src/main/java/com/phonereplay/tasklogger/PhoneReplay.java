package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
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

    @SuppressLint("StaticFieldLeak")
    private static PhoneReplayApi phoneReplayApi;
    @SuppressLint("StaticFieldLeak")
    private static PhoneReplay sInstance;
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

        private void initView(Activity activity) {
            Log.d("Instrumentation", "initView" + activity.getComponentName().toString());
            phoneReplayApi.setCurrentView(activity.getWindow().getDecorView());
            phoneReplayApi.getCurrentView().setDrawingCacheEnabled(true);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {
            Log.d("Instrumentation", "callActivityOnCreate" + activity.getComponentName().toString());
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
            Log.d("Instrumentation", "callActivityOnStart" + activity.getComponentName().toString());
            initThread(activity);
            Window window = activity.getWindow();

            if (window != null && !(window.getCallback() instanceof UserInteractionAwareCallback)) {
                window.setCallback(new UserInteractionAwareCallback(window.getCallback(), activity));
            }
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
            Log.d("YourClassName", "Screen width: " + displayMetrics.widthPixels + ", height: " + displayMetrics.heightPixels);
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            Log.d("Instrumentation", "OnResume");
            updateAndHandleScreenDimensions(activity);
            super.callActivityOnResume(activity);
        }

        @Override
        public void callActivityOnPause(Activity activity) {
            Log.d("Instrumentation", "OnPause");
            super.callActivityOnPause(activity);
        }

        @Override
        public void callActivityOnStop(Activity activity) {
            Log.d("Instrumentation", "OnStop");
            super.callActivityOnStop(activity);
        }

        @Override
        public void callActivityOnDestroy(Activity activity) {
            Log.d("Instrumentation", "OnDestroy");
            super.callActivityOnDestroy(activity);
        }

        @Override
        public void callActivityOnUserLeaving(Activity activity) {
            Log.d("Instrumentation", "OnUserLeaving");
            super.callActivityOnUserLeaving(activity);
        }
    }
}
