package com.letter.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.letter.thirsty.ThirstyApplication;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotifyDataUtil {

    private static final String TAG = "NotifyDataUtil";

    public static void getNotifyData(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                try {
                    NotifyData notifyData = gson.fromJson(response.body().string(), NotifyData.class);
                    if (notifyData != null) {
                        ThirstyApplication.setNotifyData(notifyData);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }
}
