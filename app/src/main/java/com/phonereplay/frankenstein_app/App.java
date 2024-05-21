package com.phonereplay.frankenstein_app;

import android.app.Application;

import com.phonereplay.tasklogger.PhoneReplay;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PhoneReplay.init(this, "55840540423a2256372b3f00304f01f735f695bb");

        //Smartlook smartlook = Smartlook.getInstance();
        //smartlook.getPreferences().setProjectKey("9a64208df2a90714d0c6744ee7604df64ac98914");
    }
}