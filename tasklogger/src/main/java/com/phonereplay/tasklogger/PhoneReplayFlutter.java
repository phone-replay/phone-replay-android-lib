package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;

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
        Log.d("PhoneReplayFlutter", "PhoneReplay constructor called");
    }

    public static void init(String accessKey) {
        Log.d("PhoneReplayFlutter", "init called");
        PhoneReplayFlutter.getInstance(accessKey).attachBaseContext();
    }

    public synchronized static PhoneReplayFlutter getInstance(String accessKey) {
        if (sInstance == null) {
            sInstance = new PhoneReplayFlutter(accessKey);
            Log.d("PhoneReplayFlutter", "getInstance: new instance created");
        }
        Log.d("PhoneReplayFlutter", "getInstance called");
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PhoneReplayFlutter", "onCreate called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("PhoneReplayFlutter", "onStart called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("PhoneReplayFlutter", "onResume called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("PhoneReplayFlutter", "onPause called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("PhoneReplayFlutter", "onStop called");
        // Adicione o que você deseja fazer aqui
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("PhoneReplayFlutter", "onDestroy called");
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
        Log.d("PhoneReplayFlutter", "replaceInstrumentation called");
        Reflect contextImplRef = Reflect.on(contextImpl);
        Reflect activityThreadRef = contextImplRef.field("mMainThread");
        Reflect instrumentationRef = activityThreadRef.field("mInstrumentation");
    }

    private void attachBaseContext() {
        Log.d("PhoneReplayFlutter", "attachBaseContext called");
        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
        Context contextImpl = getContextImpl(context);
        replaceInstrumentation(contextImpl);
    }

    private Context getContextImpl(Context context) {
        Log.d("PhoneReplayFlutter", "getContextImpl called");
        Context nextContext;
        while ((context instanceof ContextWrapper) &&
                (nextContext = ((ContextWrapper) context).getBaseContext()) != null) {
            context = nextContext;
        }
        return context;
    }
}
