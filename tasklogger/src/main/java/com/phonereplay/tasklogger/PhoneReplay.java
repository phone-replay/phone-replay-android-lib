package com.phonereplay.tasklogger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.phonereplay.tasklogger.reflect.Reflect;

public class PhoneReplay extends Activity {

    private static PhoneReplayApi phoneReplayApi;
    private static PhoneReplay sInstance;
    private final Context context;
    private final ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks();
    private Activity currentActivity;
    private int previousWidth;
    private int previousHeight;

    private PhoneReplay(String accessKey, Application application) {
        context = AppContext.getContext();
        phoneReplayApi = new PhoneReplayApi(context, accessKey);
        application.registerActivityLifecycleCallbacks(callbacks);
        Log.d("PhoneReplay", "PhoneReplay constructor called");
    }

    public static void init(String accessKey, Application application) {
        Log.d("PhoneReplay", "init called");
        PhoneReplay.getInstance(accessKey, application).attachBaseContext();
    }

    public synchronized static PhoneReplay getInstance(String accessKey, Application application) {
        if (sInstance == null) {
            sInstance = new PhoneReplay(accessKey, application);
            Log.d("PhoneReplay", "getInstance: new instance created");
        }
        Log.d("PhoneReplay", "getInstance called");
        return sInstance;
    }

    public synchronized static PhoneReplay getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("PhoneReplay is not initialized. Call init() first.");
        }
        return sInstance;
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

    public ActivityLifecycleCallbacks getCallbacks() {
        return callbacks;
    }

    public class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        private static final String TAG = "ActivityLifecycle";
        private Activity currentActivity;

        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            Log.d(TAG, "onActivityCreated: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            Log.d(TAG, "onActivityStarted: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            currentActivity = activity;
            Log.d(TAG, "onActivityResumed: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            Log.d(TAG, "onActivityPaused: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (currentActivity == activity) {
                currentActivity = null;
            }
            Log.d(TAG, "onActivityStopped: " + activity.getLocalClassName());
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            Log.d(TAG, "onActivitySaveInstanceState: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            Log.d(TAG, "onActivityDestroyed: " + activity.getLocalClassName());
        }
    }
}
