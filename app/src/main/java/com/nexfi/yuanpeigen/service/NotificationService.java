package com.nexfi.yuanpeigen.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.nexfi.yuanpeigen.activity.ChatActivity;

/**
 * Created by Mark on 2016/3/9.
 */
public class NotificationService extends Service {
    private static final String ACTION = "com.nexfi.yuanpeigen";
    private int avater;
    private String username;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        filter.setPriority(12345);
        registerReceiver(myReceiver, filter);
    }

    //创建通知
    public void createInform(int avatar, String username) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this);
        Intent resultIntent = new Intent(this, ChatActivity.class);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), avatar);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentTitle(username)
                .setContentText("您有新消息")
                .setLargeIcon(bitmap)
                .setSmallIcon(avatar)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis());
        manager.notify(52, notifyBuilder.build());
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = getResultExtras(true);
            username = bundle.getString("username");
            avater = bundle.getInt("avatar");
            createInform(avater, username);
        }

    };


}
