package com.phonereplay.tasklogger;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;

import java.util.Locale;

public class DeviceInfo {

    public static String getTotalStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) statFs.getBlockSize() * (long) statFs.getBlockCount();
        return formatSize(bytesAvailable);
    }


    public static String getTotalRAM(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = memoryInfo.totalMem;
        return formatSize(totalMemory);
    }


    public static String getCurrentNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "MOBILE";
            }
        }
        return "NOT CONNECTED";
    }

    public static String getLanguage() {
        return Locale.getDefault().getDisplayLanguage();
    }

    public static float getBatteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        return level / (float) scale * 100;
    }

    @SuppressLint("DefaultLocale")
    private static String formatSize(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;

        if (bytes >= gigabyte) {
            return String.format("%.2f GB", (float) bytes / gigabyte);
        } else if (bytes >= megabyte) {
            return String.format("%.2f MB", (float) bytes / megabyte);
        } else if (bytes >= kilobyte) {
            return String.format("%.2f KB", (float) bytes / kilobyte);
        } else {
            return String.format("%d Bytes", bytes);
        }
    }

    public static String getScreenResolution(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return width + "x" + height;
    }
}
