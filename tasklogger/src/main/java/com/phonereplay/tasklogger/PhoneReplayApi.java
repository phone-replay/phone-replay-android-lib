package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.phonereplay.tasklogger.exception.MyExceptionHandler;
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
    private static StopwatchUtility stopwatch = new StopwatchUtility();
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public boolean orientation = false;
    public int mainHeight = 0;
    public int mainWidth = 0;
    private View currentView;

    public PhoneReplayApi(Context context) {
        apiClientService = new PhoneReplayService(context);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(context));
        PhoneReplayApi.context = context;
    }

    public static StopwatchUtility getStopwatch() {
        if (stopwatch == null) {
            stopwatch = new StopwatchUtility();
        }
        return stopwatch;
    }

    public static Context getContext() {
        return context;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static void startRecording() {
        Toast.makeText(context, "DEV TEST: gravando video", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            gestureRecorder = new GestureRecorder();

            startRecording = true;
            mHandler.postDelayed(thread, RECORDING_INTERVAL);
            startCountUp();
        }).start();
    }

    public static void stopRecording() {
        startRecording = false;
        mHandler.removeCallbacks(thread);
        Log.d("timer", stopwatch.timer);
        stopwatch.stop();

        Toast.makeText(context, "DEV TEST: parando gravacao de video", Toast.LENGTH_LONG).show();

        DeviceModel deviceModel = new DeviceModel(context);
        if (gestureRecorder != null) {
            String summaryLog = gestureRecorder.generateSummaryLog();
            Log.d("GestureRecorderSummary", summaryLog);
        }
        new Thread(() -> {
            try {
                apiClientService.createVideo(gestureRecorder.currentSession, deviceModel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void startCountUp() {
        if (stopwatch == null) {
            stopwatch = new StopwatchUtility();
        }
        stopwatch.start();
    }

    public static void registerTouchAction(String action, float x, float y) {
        if (startRecording && currentActivity != null) {
            String activityName = currentActivity.getClass().getSimpleName();
            String targetTime = stopwatch.timer;
            String gestureType = "(" + x + ", " + y + ")";
            gestureRecorder.registerGesture(activityName, action, targetTime, gestureType);
            Log.d("ActionRegistered", "Gesture: " + gestureType + ", Time: " + targetTime);
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
                                    Bitmap bitmap = BitmapUtils.convertViewToDrawable(currentView);
                                    apiClientService.queueBytesBitmapV2(bitmap);
                                    currentView.destroyDrawingCache();
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

