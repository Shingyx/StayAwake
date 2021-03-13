package com.github.shingyx.stayawake;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class StayAwakeApplication extends Application {
    public final static String SERVICE_NOTIFICATION_CHANNEL_ID = "com.github.shingyx.stayawake.STAY_ON_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel = new NotificationChannel(
                    SERVICE_NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_description),
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
