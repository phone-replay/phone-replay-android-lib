package com.phonereplay.tasklogger;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class PhoneReplay extends Activity {

    private static PhoneReplayApi phoneReplayApi;
    private static PhoneReplay sInstance;
    private final Context context;
    private int previousWidth;
    private int previousHeight;

    private PhoneReplay(String accessKey) {
        context = AppContext.getContext();
        phoneReplayApi = new PhoneReplayApi(context, accessKey);
        Log.d("PhoneReplay", "PhoneReplay constructor called");
    }

    public static void init(String accessKey) {
        Log.d("PhoneReplay", "init called");
        PhoneReplay.getInstance(accessKey).attachBaseContext();
    }

    public synchronized static PhoneReplay getInstance(String accessKey) {
        if (sInstance == null) {
            sInstance = new PhoneReplay(accessKey);
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


    private void attachBaseContext() {
        Log.d("PhoneReplay", "attachBaseContext called");
        phoneReplayApi.initThread();
        phoneReplayApi.initHandler();
    }
}
