package com.vanta.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class C2Service extends Service {
    @Override
    public int onStartCommand(Intent i, int f, int s) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("c2", "Service", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }

        Notification n;
        if (Build.VERSION.SDK_INT >= 26)
            n = new Notification.Builder(this, "c2")
                .setContentTitle("System").setSmallIcon(android.R.drawable.stat_notify_sync).build();
        else
            n = new Notification.Builder(this)
                .setContentTitle("System").setSmallIcon(android.R.drawable.stat_notify_sync).build();

        startForeground(1, n);
        return START_STICKY;
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
