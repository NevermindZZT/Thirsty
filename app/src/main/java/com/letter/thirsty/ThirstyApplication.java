package com.letter.thirsty;

import android.app.Application;
import android.content.Intent;

import com.letter.utils.Notify;

public class ThirstyApplication extends Application {

    private static Notify notify;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, CoreService.class);
        startService(intent);
    }

    public static Notify getNotify() {
        return notify;
    }

    public static void setNotify(Notify notify) {
        ThirstyApplication.notify = notify;
    }
}
