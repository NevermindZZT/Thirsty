package com.letter.thirsty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ThirstyReceiver extends BroadcastReceiver {

    private static final String TAG = "ThirstyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent intent1 = new Intent(context, CoreService.class);
        context.startService(intent1);
        Log.d(TAG, "broadcast receive");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
