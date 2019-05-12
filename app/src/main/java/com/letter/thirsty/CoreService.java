package com.letter.thirsty;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.letter.utils.NotifyData;
import com.letter.utils.NotifyDataUtil;

import java.util.Calendar;
import java.util.Random;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;


public class CoreService extends Service {

    private static final String TAG = "CoreService";

    private static final int[] DRINK_HOUR = {6, 8, 11, 12, 15, 17, 22};
    private static final int[] DRINK_MINUTE = {30, 30, 0, 50, 0, 30, 0};
    private static final long MILLIS_A_MINUTE = 60 * 1000;
    private static final long MILLIS_AN_HOUR = 60 * MILLIS_A_MINUTE;
    private static final long MILLIS_A_DAY = 24 * MILLIS_AN_HOUR;

    private static final int APP_DESK_ID = 1;
    private static final int NOTIFY_ID = 2;

    private static final String INTENT_KEY = "notify";

    private static final int NOTIFY_NONE = 0;
    private static final int NOTIFY_NEW = 1;
    private static final int NOTIFY_DELAY = 2;
    private static final int NOTIFY_DONE = 3;

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");


        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, notifyIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_app))
                .setContentTitle(getString(R.string.notification_content_app_title))
                .setContentText(getString(R.string.notification_content_app_text))
                .setSmallIcon(R.mipmap.ic_notify)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent1);
        startForeground(APP_DESK_ID, notification.build());

        int type = NOTIFY_DONE;

        if (intent != null) {
            type = intent.getIntExtra(INTENT_KEY, NOTIFY_NONE);
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            Log.d(TAG, "intent: " + type);
            switch (type) {
                case NOTIFY_NEW:
                    thirstyNotify();
                    break;
                case NOTIFY_DELAY:
                    manager.cancel(NOTIFY_ID);
                    break;
                case NOTIFY_DONE:
                    manager.cancel(NOTIFY_ID);
                    break;
                default:
                    break;
            }
        }

        Intent intent1 = new Intent(getApplicationContext(), CoreService.class);
        intent1.putExtra(INTENT_KEY, NOTIFY_NEW);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent1, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (type == NOTIFY_DELAY) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 5 * MILLIS_A_MINUTE, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, getNextAlarm().getTimeInMillis(), pendingIntent);
        }

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

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
    }

    private void thirstyNotify() {
        String notifyContent = getString(R.string.notification_content_text);
        NotifyData notifyData = ThirstyApplication.getNotifyData();

        if (notifyData != null) {
            Random random = new Random();
            int num = random.nextInt(notifyData.getNotifyList().size()) % (notifyData.getNotifyList().size() + 1);
            notifyContent = notifyData.getNotifyList().get(num).getData();
        }

        final String title = getString(R.string.notification_content_title) + "  " + getCurrentTime();
        final String content = notifyContent;
        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent delayIntent = new Intent(getApplicationContext(), CoreService.class);
                delayIntent.putExtra(INTENT_KEY, NOTIFY_DELAY);
                PendingIntent delayPI = PendingIntent.getService(getApplicationContext(), NOTIFY_DELAY, delayIntent, FLAG_UPDATE_CURRENT);

                Intent doneIntent = new Intent(getApplicationContext(), CoreService.class);
                doneIntent.putExtra(INTENT_KEY, NOTIFY_DONE);
                PendingIntent donePI = PendingIntent.getService(getApplicationContext(), NOTIFY_DONE, doneIntent, FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                        getString(R.string.notification_channel_id_notify))
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.mipmap.ic_notify)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_juice))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .addAction(R.drawable.bg_notify_button, getString(R.string.notification_delay_button), delayPI)
                        .addAction(R.drawable.bg_notify_button, getString(R.string.notification_done_button), donePI);

                NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                manager.notify(NOTIFY_ID, builder.build());
            }
        }).start();
    }
}
