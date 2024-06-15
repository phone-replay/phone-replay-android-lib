package com.phonereplay.frankenstein_app;

import android.app.Application;

import com.phonereplay.tasklogger.PhoneReplay;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PhoneReplay.init("9829270026e0d6bb6eb3144225dd93deef6eb3c8");


        //Smartlook smartlook = Smartlook.getInstance();
        //smartlook.getPreferences().setProjectKey("d5e67b16d758bcc7b3aae16f9485b9826469b244");
    }
}