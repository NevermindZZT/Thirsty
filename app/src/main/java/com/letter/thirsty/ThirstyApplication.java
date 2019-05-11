package com.letter.thirsty;

import android.app.Application;
import android.content.Intent;

import com.letter.utils.NotifyData;

public class ThirstyApplication extends Application {

    private static NotifyData notifyData;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, CoreService.class);
        startService(intent);
    }

    public static NotifyData getNotifyData() {
        return notifyData;
    }

    public static void setNotifyData(NotifyData notifyData) {
        ThirstyApplication.notifyData = notifyData;
    }
}
