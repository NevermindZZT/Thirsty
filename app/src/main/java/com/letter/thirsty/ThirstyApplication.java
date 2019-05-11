package com.letter.thirsty;

import android.app.Application;
import android.content.Intent;

public class ThirstyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, CoreService.class);
        startService(intent);
    }
}
