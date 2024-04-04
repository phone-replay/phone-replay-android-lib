package com.phonereplay.frankenstein_app;

import android.app.Application;
import android.content.Context;

import com.phonereplay.tasklogger.PhoneReplay;

public class App extends Application {

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PhoneReplay.init(this, "6e55b630-88a2-4545-be55-65bd68972d6b");
        sApplication = this;
    }
}