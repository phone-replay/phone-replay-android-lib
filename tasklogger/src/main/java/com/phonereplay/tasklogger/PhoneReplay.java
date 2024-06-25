package com.phonereplay.tasklogger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import io.flutter.embedding.android.FlutterActivity;

public class PhoneReplay extends Activity {

    private static PhoneReplayApi phoneReplayApi;
    private static PhoneReplay sInstance;
    private static Context context = null;
    private static String accessKey = null;

    private PhoneReplay(String accessKey) {
        context = AppContext.getContext();
        PhoneReplay.accessKey = accessKey;
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

    private void attachBaseContext() {
        try {
            Activity activity = MainActivityFetcher.getOnlyMainActivity();
            if (activity instanceof FlutterActivity) {
                phoneReplayApi = new PhoneReplayApi(context, accessKey, "FLUTTER");
                Log.d("ActivityInstance", "activity is flutter instance");
            }

            Class<?> flutterActivityClass = Class.forName("io.flutter.embedding.android.FlutterActivity");

            Log.d("ActivityInstance", "Class name: " + flutterActivityClass.getName());

            if (flutterActivityClass.isInstance(activity)) {
                Log.d("ActivityInstance", "activity is flutter instance");
            } else {
                Log.d("ActivityInstance", "activity isn't flutter instance");
            }
        } catch (ClassNotFoundException e) {
            Log.d("ActivityInstance", "Class not found: " + e.getMessage());
            e.printStackTrace();
        }
        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
    }
}
