package com.github.shingyx.stayawake;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class StayAwakeApplication extends Application {
    public final static String SERVICE_NOTIFICATION_CHANNEL_ID = "com.github.shingyx.stayawake.STAY_AWAKE_SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();

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
