package com.phonereplay.tasklogger;

import android.app.Application;
import android.content.Context;

public class PhoneReplayApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PhoneReplay.init(this, "6e55b630-88a2-4545-be55-65bd68972d6b");
    }
}
