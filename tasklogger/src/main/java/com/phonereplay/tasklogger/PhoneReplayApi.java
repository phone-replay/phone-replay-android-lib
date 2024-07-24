package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.phonereplay.tasklogger.service.PhoneReplayService;
import com.phonereplay.tasklogger.utils.BitmapUtils;

import java.io.IOException;

public class PhoneReplayApi {

    /**
     * Intervalo de captura em milissegundos.
     * Certifique-se de que o intervalo de captura e a taxa de quadros do vídeo (SERVIDOR PYTHON) estão alinhados.
     * Se você está capturando a cada 100 milliseconds, isso seria aproximadamente 10 quadros por segundo
     * (considerando que 1 segundo = 1000 milliseconds).
     */
    private static final int RECORDING_INTERVAL = 100;
    public static boolean startRecording = false;
    private static Thread thread;
    private static Handler mHandler;
    @SuppressLint("StaticFieldLeak")
    private static PhoneReplayService apiClientService;
    @SuppressLint("StaticFieldLeak")
    private static Activity currentActivity;
    @SuppressLint("StaticFieldLeak")
    private static GestureRecorder gestureRecorder;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static String projectKey;
    private static long startTime;
    private static long endTime;
    public boolean orientation = false;
    public int mainHeight = 0;
    public int mainWidth = 0;
    private View currentView;

    public PhoneReplayApi(Context context, String accessKey) {
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(context));
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
            gestureRecorder = new GestureRecorder();
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
            Log.d("RecordingDuration", "Duração da gravação: " + duration + " milissegundos");

            DeviceModel deviceModel = new DeviceModel(context);
            if (gestureRecorder != null) {
                String summaryLog = gestureRecorder.generateSummaryLog();
                Log.d("GestureRecorderSummary", summaryLog);
            }
            new Thread(() -> {
                try {
                    apiClientService.createVideo(gestureRecorder.currentSession, deviceModel, projectKey, duration);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public static void registerTouchAction(String action, float x, float y) {
        if (startRecording && currentActivity != null) {
            String activityName = currentActivity.getClass().getSimpleName();

            long currentTime = System.currentTimeMillis();
            long timeSinceStart = currentTime - startTime;
            String gestureType = "(" + x + ", " + y + ")";
            gestureRecorder.registerGesture(activityName, action, String.valueOf(timeSinceStart), gestureType);
            Log.d("ActionRegistered", "Gesture: " + gestureType + ", Time: " + timeSinceStart);
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
        Log.d("ViewLogger", "Current View: " + currentView.toString());
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

