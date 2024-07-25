package com.phonereplay.tasklogger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;

import com.phonereplay.tasklogger.service.PhoneReplayService;
import com.phonereplay.tasklogger.utils.BitmapUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PhoneReplayApi {

    private static final int RECORDING_INTERVAL = 100;
    // Mapa estático para armazenar os logs de gestos de todas as atividades
    private static final Map<String, ActivityGesture> activityGestureLogs = new HashMap<>();
    public static boolean startRecording = false;
    private static Thread thread;
    private static Handler mHandler;
    private static PhoneReplayService apiClientService;
    private static Activity currentActivity;
    private static Context context;
    private static String projectKey;
    private static long startTime;
    private static long endTime;
    public boolean orientation = false;
    public int mainHeight = 0;
    public int mainWidth = 0;
    private View currentView;

    public PhoneReplayApi(Context context, String accessKey) {
        PhoneReplayApi.context = context;
        projectKey = accessKey;
        apiClientService = new PhoneReplayService();
    }

    public static Context getContext() {
        return context;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static void startRecording() {
        new Thread(() -> {
            startRecording = true;
            startTime = System.currentTimeMillis();
            mHandler.postDelayed(thread, RECORDING_INTERVAL);
        }).start();
    }

    public static void stopRecording() {
        if (startRecording) {
            startRecording = false;
            endTime = System.currentTimeMillis();
            mHandler.removeCallbacks(thread);
            long duration = endTime - startTime;
            DeviceModel deviceModel = new DeviceModel(context);
            new Thread(() -> {
                try {
                    apiClientService.createVideo(getActivityGesture(), deviceModel, projectKey, duration);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public static void registerTouchAction(String action, float x, float y, Gesture currentGesture) {
        if (startRecording && currentActivity != null) {
            long currentTime = System.currentTimeMillis();
            long timeSinceStart = currentTime - startTime;
            String gestureType = "(" + x + ", " + y + ")";
            if (currentGesture != null) {
                currentGesture.addAction(action, String.valueOf(timeSinceStart), gestureType);
            }
        }
    }

    public static Map<String, ActivityGesture> getActivityGesture() {
        return activityGestureLogs;
    }

    public static void addActivityGesture(String activityName, Gesture gesture) {
        ActivityGesture log = activityGestureLogs.get(activityName);
        if (log != null) {
            log.addGesture(gesture);
        } else {
            log = new ActivityGesture(activityName);
            log.addGesture(gesture);
            activityGestureLogs.put(activityName, log);
        }
    }

    public void setMainHeight(int mainHeight, int mainWidth) {
        if (this.mainHeight == 0) {
            this.mainHeight = mainHeight;
            this.mainWidth = mainWidth;
        }
    }

    public View getCurrentView() {
        return currentView;
    }

    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public Thread getThread() {
        return thread;
    }

    public void initHandler() {
        mHandler = new Handler();
    }

    public void initThread() {
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    new Thread() {
                        @Override
                        public void run() {
                            if (startRecording) {
                                try {
                                    if (currentView != null) {
                                        Bitmap bitmap = BitmapUtils.convertViewToDrawable(currentView);
                                        apiClientService.queueBytesBitmap(bitmap, true);
                                        currentView.destroyDrawingCache();
                                    } else {
                                        System.err.println("Error: currentView is null");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                super.run();
                            }
                        }
                    }.start();
                    if (startRecording) {
                        mHandler.postDelayed(thread, RECORDING_INTERVAL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
