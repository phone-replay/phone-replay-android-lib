package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.phonereplay.tasklogger.service.PhoneReplayService;
import com.phonereplay.tasklogger.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.android.FlutterView;

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
    private static String projectKey;
    private static long startTime;
    private static long endTime;
    private static View currentView;
    public boolean orientation = false;
    public int mainHeight = 0;
    public int mainWidth = 0;

    public PhoneReplayApi(Context context, String accessKey) {
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(context));
        PhoneReplayApi.context = context;
        projectKey = accessKey;
        apiClientService = new PhoneReplayService();
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
        Activity mainActivity = MainActivityFetcher.getMainActivity();
        assert mainActivity != null;
        initThread(mainActivity);
        /*
        new Thread(() -> {
            boolean validateAccessKey = apiClientService.validateAccessKey(projectKey);
            if (validateAccessKey) {
                gestureRecorder = new GestureRecorder();
                startRecording = true;
                startTime = System.currentTimeMillis();
                mHandler.postDelayed(thread, RECORDING_INTERVAL);
                startCountUp();
            }
        }).start();
         */

    }

    public static void stopRecording() {
        if (startRecording) {
            startRecording = false;
            endTime = System.currentTimeMillis();
            mHandler.removeCallbacks(thread);
            Log.d("timer", stopwatch.timer);
            stopwatch.stop();

            // Calcular a duração da gravação
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

    private static void initView(Activity activity) {
        String TAG = "initView";

        Log.d(TAG, "Initializing view for activity: " + activity.getClass().getName());
        if (activity instanceof FlutterActivity) {
            FlutterActivity flutterActivity = (FlutterActivity) activity;
            ViewGroup rootView = (ViewGroup) flutterActivity.findViewById(android.R.id.content);
            if (rootView != null) {
                Log.d(TAG, "Root view found with child count: " + rootView.getChildCount());
                for (int i = 0; i < rootView.getChildCount(); i++) {
                    View child = rootView.getChildAt(i);
                    Log.d(TAG, "Child view at index " + i + ": " + child.getClass().getName());
                    if (child instanceof FlutterView) {
                        currentView = child;
                        Log.d(TAG, "FlutterView found and set as current view");
                        break;
                    }
                }
            } else {
                Log.e(TAG, "Root view is null");
            }
        } else {
            currentView = activity.getWindow().getDecorView();
            Log.d(TAG, "DecorView set as current view");
        }

        if (currentView != null) {
            currentView.setDrawingCacheEnabled(true);
            currentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> captureViewBitmap(currentView));
        } else {
            Log.e(TAG, "Current view is null");
        }
    }

    private static byte[] writeImageFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 1, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static void captureViewBitmap(View view) {
        try {
            Bitmap currentBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(currentBitmap);
            view.draw(canvas);

            byte[] a = writeImageFromBitmap(currentBitmap);
            String base64String = Base64.encodeToString(a, Base64.DEFAULT);
            Log.d("captureViewBitmap", "base64String " + base64String);
        } catch (Exception e) {
            Log.e("captureViewBitmap", "Failed to capture bitmap", e);
        }
    }

    private static void initThread(Activity activity) {
        Log.d("TaskLoggerInstrumentation", "initThread called with activity: " + activity.getClass().getSimpleName());
        Window window = activity.getWindow();

        if (window != null && !(window.getCallback() instanceof UserInteractionAwareCallback)) {
            window.setCallback(new UserInteractionAwareCallback(window.getCallback(), activity));
        }
        initView(activity);
    }

    public void setMainHeight(int mainHeight, int mainWidth) {
        if (this.mainHeight == 0) {
            this.mainHeight = mainHeight;
            this.mainWidth = mainWidth;
        }
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
                                        currentView.setDrawingCacheEnabled(true);
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

