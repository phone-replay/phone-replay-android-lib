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

import io.flutter.embedding.android.FlutterActivity;

public class PhoneReplayFlutter extends FlutterActivity {

    @SuppressLint("StaticFieldLeak")
    private static PhoneReplayApi phoneReplayApi;
    @SuppressLint("StaticFieldLeak")
    private static PhoneReplayFlutter sInstance;
    private final Context context;
    private Activity currentActivity;
    private int previousWidth;
    private int previousHeight;

    private PhoneReplayFlutter(String accessKey) {
        context = AppContext.getContext();
        phoneReplayApi = new PhoneReplayApi(context, accessKey);
        Log.d("PhoneReplay", "PhoneReplay constructor called");
    }

    public static void init(String accessKey) {
        Log.d("PhoneReplay", "init called");
        PhoneReplayFlutter.getInstance(accessKey).attachBaseContext();
    }

    public synchronized static PhoneReplayFlutter getInstance(String accessKey) {
        if (sInstance == null) {
            sInstance = new PhoneReplayFlutter(accessKey);
            Log.d("PhoneReplay", "getInstance: new instance created");
        }
        Log.d("PhoneReplay", "getInstance called");
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyFlutterActivity", "onCreate called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MyFlutterActivity", "onStart called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyFlutterActivity", "onResume called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MyFlutterActivity", "onPause called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MyFlutterActivity", "onStop called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MyFlutterActivity", "onDestroy called");
        // Adicione o que você deseja fazer aqui
    }

    private void updateDimensions(int width, int height) {
        Log.d("PhoneReplay", "updateDimensions called with width: " + width + " and height: " + height);
        if (previousWidth != 0) {
            phoneReplayApi.orientation = true;
        }
        previousWidth = width;
        previousHeight = height;
    }

    public Activity getCurrentActivity() {
        Log.d("PhoneReplay", "getCurrentActivity called");
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        Log.d("PhoneReplay", "setCurrentActivity called with activity: " + activity.getClass().getSimpleName());
        this.currentActivity = activity;
        PhoneReplayApi.setCurrentActivity(activity);
    }

    private void replaceInstrumentation(Context contextImpl) {
        Log.d("PhoneReplay", "replaceInstrumentation called");
        Reflect contextImplRef = Reflect.on(contextImpl);
        Reflect activityThreadRef = contextImplRef.field("mMainThread");
        Reflect instrumentationRef = activityThreadRef.field("mInstrumentation");
        TaskLoggerInstrumentation newInstrumentation = new TaskLoggerInstrumentation(instrumentationRef.get());
        activityThreadRef.set("mInstrumentation", newInstrumentation);
    }

    private void attachBaseContext() {
        Log.d("PhoneReplay", "attachBaseContext called");
        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
        Context contextImpl = getContextImpl(context);
        replaceInstrumentation(contextImpl);
    }

    private Context getContextImpl(Context context) {
        Log.d("PhoneReplay", "getContextImpl called");
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
            Log.d("TaskLoggerInstrumentation", "Constructor called");
            this.base = base;
            instrumentRef = Reflect.on(base);
        }

        private void initThread(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "initThread called with activity: " + activity.getClass().getSimpleName());
            if (!activity.equals(getCurrentActivity())) {
                phoneReplayApi.getmHandler().removeCallbacks(phoneReplayApi.getThread());
                phoneReplayApi.getmHandler().postDelayed(phoneReplayApi.getThread(), 100);
                setCurrentActivity(activity);
            }
            initView(activity);
        }

        private void initView(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "initView called with activity: " + activity.getClass().getSimpleName());
            phoneReplayApi.setCurrentView(activity.getWindow().getDecorView());
            phoneReplayApi.getCurrentView().setDrawingCacheEnabled(true);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle bundle) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnCreate called with activity: " + activity.getClass().getSimpleName());
            initThread(activity);
            super.callActivityOnCreate(activity, bundle);
        }

        @Override
        public void callActivityOnNewIntent(Activity activity, Intent intent) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnNewIntent called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnNewIntent(activity, intent);
        }

        @Override
        public void callActivityOnRestart(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnRestart called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnRestart(activity);
        }

        @Override
        public void callActivityOnStart(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnStart called with activity: " + activity.getClass().getSimpleName());
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
            Log.d("TaskLoggerInstrumentation", "updateAndHandleScreenDimensions called with activity: " + activity.getClass().getSimpleName());
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
            Log.d("TaskLoggerInstrumentation", "Screen width: " + displayMetrics.widthPixels + ", height: " + displayMetrics.heightPixels);
        }

        @Override
        public void callActivityOnResume(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnResume called with activity: " + activity.getClass().getSimpleName());
            updateAndHandleScreenDimensions(activity);
            super.callActivityOnResume(activity);
        }

        @Override
        public void callActivityOnPause(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnPause called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnPause(activity);
        }

        @Override
        public void callActivityOnStop(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnStop called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnStop(activity);
        }

        @Override
        public void callActivityOnDestroy(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnDestroy called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnDestroy(activity);
        }

        @Override
        public void callActivityOnUserLeaving(Activity activity) {
            Log.d("TaskLoggerInstrumentation", "callActivityOnUserLeaving called with activity: " + activity.getClass().getSimpleName());
            super.callActivityOnUserLeaving(activity);
        }
    }
}
