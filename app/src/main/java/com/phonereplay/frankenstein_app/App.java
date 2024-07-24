package com.phonereplay.frankenstein_app;

import android.app.Application;

import com.phonereplay.tasklogger.PhoneReplay;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PhoneReplay.init("9829270026e0d6bb6eb3144225dd93deef6eb3c8");
    }
}