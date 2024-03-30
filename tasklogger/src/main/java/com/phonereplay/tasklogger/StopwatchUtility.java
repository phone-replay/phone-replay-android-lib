package com.phonereplay.tasklogger;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class StopwatchUtility {
    private final Handler handler;
    public String timer;
    private int hours, minutes, seconds, milliSeconds;
    private long millisecondTime, startTime, timeBuff, updateTime = 0L;
    private boolean isRunning = false;
    private StopwatchListener listener;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            millisecondTime = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuff + millisecondTime;

            hours = (int) (updateTime / (3600 * 1000));
            minutes = (int) (updateTime / (60 * 1000)) % 60;
            seconds = (int) (updateTime / 1000) % 60;
            milliSeconds = (int) (updateTime % 1000);
            isRunning = true;

            onTick(hours, minutes, seconds, milliSeconds);

            handler.postDelayed(this, 0);
        }
    };

    public StopwatchUtility() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        if (!isRunning) {
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            timeBuff += millisecondTime;
            handler.removeCallbacks(runnable);
            isRunning = false;
        }
        reset();
    }

    private void reset() {
        millisecondTime = 0L;
        startTime = 0L;
        timeBuff = 0L;
        updateTime = 0L;
        hours = 0;
        minutes = 0;
        seconds = 0;
        milliSeconds = 0;
        timer = "00:00:00.00";
        handler.removeCallbacks(runnable);
        isRunning = false;
    }

    public void setStopwatchListener(StopwatchListener listener) {
        this.listener = listener;
    }

    private void onTick(int hours, int minutes, int seconds, int milliSeconds) {
        double fractionalSeconds = seconds + (milliSeconds / 1000.0);
        timer = String.format("%02d:%02d:%05.2f", hours, minutes, fractionalSeconds);
        Log.d("onTick", "onTick: " + timer);
        if (listener != null) {
            listener.onTick(timer);
        }
    }

    public interface StopwatchListener {
        void onTick(String time);
    }
}
