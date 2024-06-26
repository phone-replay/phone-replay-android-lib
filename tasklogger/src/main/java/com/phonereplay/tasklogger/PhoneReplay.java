package com.phonereplay.tasklogger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

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
        phoneReplayApi = new PhoneReplayApi(context, accessKey, "FLUTTER");
        Log.d("ActivityInstance", "activity is flutter instance");

        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
    }
}
