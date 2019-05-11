package com.letter.thirsty;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.letter.utils.NotifyData;
import com.letter.utils.NotifyDataUtil;

import java.util.Calendar;
import java.util.Random;

import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

public class CoreService extends Service {

    private static final String TAG = "CoreService";

    private static final int[] DRINK_HOUR = {6, 8, 11, 12, 15, 17, 22};
    private static final int[] DRINK_MINUTE = {30, 30, 0, 50, 0, 30, 0};
    private static final long MILLIS_AN_HOUR = 60 * 60 * 1000;
    private static final long MILLIS_A_DAY = 24 * MILLIS_AN_HOUR;

    private static final int APP_DESK_ID = 1;
    private static final int NOTIFY_ID = 2;

    private static final String INTENT_KEY = "notify";

    public CoreService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel(getString(R.string.notification_channel_id_app),
                getString(R.string.notification_channel_name_app),
                NotificationManager.IMPORTANCE_LOW);
        createNotificationChannel(getString(R.string.notification_channel_id_notify),
                getString(R.string.notification_channel_name_notify),
                NotificationManager.IMPORTANCE_HIGH);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        stopForeground(true);

//        Intent intent = new Intent(getApplicationContext(), CoreService.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");


        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, notifyIntent, 0);

        Notification notification = new Notification.Builder(this, getString(R.string.notification_channel_id_app))
                .setContentTitle(getString(R.string.notification_content_app_title))
                .setContentText(getString(R.string.notification_content_app_text))
                .setSmallIcon(R.mipmap.ic_water)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentIntent(pendingIntent1)
                .build();
        startForeground(APP_DESK_ID, notification);

        if (intent != null
                && intent.getIntExtra(INTENT_KEY, 0) == 1) {
            thirstyNotify();
        }

        Intent intent1 = new Intent(getApplicationContext(), CoreService.class);
        intent1.putExtra(INTENT_KEY, 1);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent1, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getNextAlarm().getTimeInMillis(), pendingIntent);

        NotifyDataUtil.getNotifyData("https://raw.githubusercontent.com/NevermindZZT/Thirsty/master/notify.json");

        return START_STICKY;

//        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    private Calendar getNextAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() + 0 * MILLIS_AN_HOUR);
        if (calendar.get(Calendar.HOUR_OF_DAY) > DRINK_HOUR[DRINK_HOUR.length - 1]
                || (calendar.get(Calendar.HOUR_OF_DAY) == DRINK_HOUR[DRINK_HOUR.length - 1]
                && calendar.get(Calendar.MINUTE) >= DRINK_MINUTE[DRINK_MINUTE.length - 1])) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + MILLIS_A_DAY);
            calendar.set(Calendar.HOUR_OF_DAY, DRINK_HOUR[0]);
            calendar.set(Calendar.MINUTE, DRINK_MINUTE[0]);
        } else {
            for (int i = 0; i < DRINK_HOUR.length; i++) {
                if (calendar.get(Calendar.HOUR_OF_DAY) < DRINK_HOUR[i]
                        || (calendar.get(Calendar.HOUR_OF_DAY) == DRINK_HOUR[i]
                        && calendar.get(Calendar.MINUTE) < DRINK_MINUTE[i])) {
                    calendar.set(Calendar.HOUR_OF_DAY, DRINK_HOUR[i]);
                    calendar.set(Calendar.MINUTE, DRINK_MINUTE[i]);
                    break;
                }
            }
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("CoreService", "onStartCommand: " + calendar.get(Calendar.YEAR) + "-"
                + (calendar.get(Calendar.MONTH) + 1) + "-"
                + calendar.get(Calendar.DAY_OF_MONTH) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE));
        calendar.setTimeInMillis(calendar.getTimeInMillis() - 0 * MILLIS_AN_HOUR);
        return calendar;
    }

    private void thirstyNotify() {
        String notifyContent = getString(R.string.notification_content_text);
        NotifyData notifyData = ThirstyApplication.getNotifyData();

        if (notifyData != null) {
            Random random = new Random();
            int num = random.nextInt(notifyData.getNotifyList().size()) % (notifyData.getNotifyList().size() + 1);
            notifyContent = notifyData.getNotifyList().get(num).getData();
        }

        final String content = notifyContent;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Notification notification = new Notification.Builder(getApplicationContext(),
                        getString(R.string.notification_channel_id_notify))
                        .setContentTitle(getString(R.string.notification_content_title))
                        .setContentText(content)
                        .setSmallIcon(R.mipmap.ic_water)
                        .setWhen(System.currentTimeMillis())
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                        .setAutoCancel(true)
                        .build();

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(NOTIFY_ID, notification);
            }
        }).start();
    }
}
