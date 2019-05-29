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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.letter.utils.Notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * 核心服务
 * @author Letter(NevermindZZT@gmail.com)
 * @version 1.0
 */
public class CoreService extends Service {

    private static final String TAG = "CoreService";

    private static final long MILLIS_A_MINUTE = 60 * 1000;
    private static final long MILLIS_AN_HOUR = 60 * MILLIS_A_MINUTE;
    private static final long MILLIS_A_DAY = 24 * MILLIS_AN_HOUR;

    private static final int APP_DESK_ID = -1;
    private static final int NOTIFY_ID = 2;

    private static final String INTENT_KEY = "notify";
    private static final String INTENT_ID = "alarm_id";
    private static final String INTENT_TITLE = "title";
    private static final String INTENT_CONTENT = "content";

    private static final int NOTIFY_NONE = 0;
    private static final int NOTIFY_NEW = 1;
    private static final int NOTIFY_DELAY = 2;
    private static final int NOTIFY_DONE = 3;
    private static final int NOTIFY_REFRESH = 4;

    private static final int REQUEST_RETRY = -1;
    private static final int REQUEST_DELAY = -2;
    private static final int REQUEST_DONE = -3;
    private static final int REQUEST_DELAY_NOTIFY = -4;

    private static final String NOTIFY_URL = "https://raw.githubusercontent.com/NevermindZZT/Thirsty/master/notify_new.json";

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
                NotificationManager.IMPORTANCE_MIN);
        createNotificationChannel(getString(R.string.notification_channel_id_notify),
                getString(R.string.notification_channel_name_notify),
                NotificationManager.IMPORTANCE_MAX);

        getNotify(NOTIFY_URL);
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

        if (intent != null) {
            int type = intent.getIntExtra(INTENT_KEY, NOTIFY_NONE);
            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            Log.d(TAG, "intent: " + type);
            switch (type) {
                case NOTIFY_NEW:
//                    thirstyNotify();
                    setThristyNotify(intent.getStringExtra(INTENT_TITLE), intent.getStringExtra(INTENT_CONTENT));
                    break;
                case NOTIFY_DELAY:
                    manager.cancel(NOTIFY_ID);
                    setDelayAlarm(intent.getStringExtra(INTENT_TITLE),
                            intent.getStringExtra(INTENT_CONTENT));
                    break;
                case NOTIFY_DONE:
                    manager.cancel(NOTIFY_ID);
                    break;
                case NOTIFY_REFRESH:
                    getNotify(NOTIFY_URL);
                    break;
                default:
                    break;
            }
        }

        setNotifyAlarm();

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

    /**
     * 获取基础时间戳(当天0点时间戳)
     * @return 时间戳
     */
    private long getBaseTimeMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 读取Assets目录下文件文本
     * @param fileName 文件名
     * @return 文件文本
     */
    private String getAssetsText(String fileName) {
        StringBuilder buffer = new StringBuilder();
        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                buffer.append(string);
                buffer.append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return buffer.toString();
    }

    /**
     * 获取通知数据
     * @param url 请求链接
     */
    private void getNotify(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                try {
                    String content = response.body().string();
//                    Log.d(TAG, content);
                    Notify notify = gson.fromJson(content, Notify.class);
                    if (notify != null) {
                        ThirstyApplication.setNotify(notify);
                        setNotifyAlarm();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Gson gson = new Gson();
                try {
                    String content = getAssetsText("notify.json");
//                    Log.d(TAG, "read file: " + content);
                    if (!content.equals("")) {
                        Notify notify = gson.fromJson(content, Notify.class);
                        if (notify != null) {
                            ThirstyApplication.setNotify(notify);
                            Log.d(TAG, "set notify:" + notify.getNotify().size());
                        }
                    } else {
                        Log.e(TAG, "read file error");
                    }
                } catch (Exception e1) {
                    Log.e(TAG, "", e1);
                }
            }
        });
    }

    /**
     * 显示通知
     * @param title 通知标题
     * @param content 通知内容
     */
    private void setThristyNotify(String title, String content) {
        Intent delayIntent = new Intent(getApplicationContext(), CoreService.class);
        delayIntent.putExtra(INTENT_KEY, NOTIFY_DELAY);
        delayIntent.putExtra(INTENT_TITLE, title);
        delayIntent.putExtra(INTENT_CONTENT, content);
        PendingIntent delayPI = PendingIntent.getService(getApplicationContext(), REQUEST_DELAY,
                delayIntent, FLAG_UPDATE_CURRENT);

        Intent doneIntent = new Intent(getApplicationContext(), CoreService.class);
        doneIntent.putExtra(INTENT_KEY, NOTIFY_DONE);
        doneIntent.putExtra(INTENT_TITLE, title);
        doneIntent.putExtra(INTENT_CONTENT, content);
        PendingIntent donePI = PendingIntent.getService(getApplicationContext(), REQUEST_DONE,
                doneIntent, FLAG_UPDATE_CURRENT);

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

    /**
     * 设置Alarm
     * @param alarmId id
     * @param type 类型
     * @param title 标题
     * @param content 内容
     * @param time 时间
     */
    private void setAlarm(int alarmId, int type, @Nullable String title, @Nullable String content, long time) {
        Intent intent = new Intent(getApplicationContext(), CoreService.class);
        intent.putExtra(INTENT_KEY, type);
        if (title != null) {
            intent.putExtra(INTENT_TITLE, title);
        }
        if (content != null) {
            intent.putExtra(INTENT_CONTENT, content);
        }
        intent.putExtra(INTENT_ID, alarmId);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), alarmId,
                intent, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    /**
     * 设置通知Alarm
     */
    private void setNotifyAlarm() {
        Notify notify = ThirstyApplication.getNotify();

        if (Calendar.getInstance().getTimeInMillis() < getBaseTimeMillis() + 12 * MILLIS_AN_HOUR) {
            setAlarm(-1, NOTIFY_REFRESH, null, null,
                    getBaseTimeMillis() + 12 * MILLIS_AN_HOUR);
        } else {
            setAlarm(-1, NOTIFY_REFRESH, null, null,
                    getBaseTimeMillis() + MILLIS_A_DAY);
        }

        if (notify == null || notify.getNotify().size() == 0) {
            Intent intent = new Intent(getApplicationContext(), CoreService.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_RETRY,
                    intent, FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            long time = Calendar.getInstance().getTimeInMillis() + 1000 * 10;
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            return;
        }

        Random random = new Random();
        int size = notify.getNotify().size();

        for (int i = 0; i < size; i++) {
            if (notify.getNotify().get(i).getTime() < 0) {

            } else {
                String title = notify.getNotify().get(i).getTitle();
                int contentNum = notify.getNotify().get(i).getContent().size();
                int contentIndex = random.nextInt(contentNum) % (contentNum + 1);
                String content = notify.getNotify().get(i).getContent().get(contentIndex);

                long time = getBaseTimeMillis() + notify.getNotify().get(i).getTime();
                if (time < Calendar.getInstance().getTimeInMillis()) {
                    time += MILLIS_A_DAY;
                }
                setAlarm(i, NOTIFY_NEW, title, content, time);
            }
        }
    }

    /**
     * 设置延迟提醒闹钟
     * @param title 标题
     * @param content 内容
     */
    private void setDelayAlarm(String title, String content) {
        long time = Calendar.getInstance().getTimeInMillis() + 5 * MILLIS_A_MINUTE;
        setAlarm(REQUEST_DELAY_NOTIFY, NOTIFY_NEW, title, content, time);
    }
}
