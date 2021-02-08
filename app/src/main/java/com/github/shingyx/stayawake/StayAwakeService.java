package com.github.shingyx.stayawake;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class StayAwakeService extends TileService {
    public final static String ACTION_TOGGLE_STAY_AWAKE = "com.github.shingyx.stayawake.TOGGLE_STAY_AWAKE";
    public final static String ACTION_STOP_KEEPING_SCREEN_ON = "com.github.shingyx.stayon.STOP_KEEPING_SCREEN_ON";
    public final static String NOTIFICATION_CHANNEL_ID = "com.github.shingyx.lockwidget.STAY_ON_SERVICE";

    private final static String WAKE_LOCK_TAG = "com.github.shingyx.stayawake:WAKE_LOCK";

    private NotificationManager notificationManager;
    private PowerManager.WakeLock wakeLock;
    private ScreenOffReceiver screenOffReceiver;

    @SuppressWarnings("deprecation")// SCREEN_DIM_WAKE_LOCK is deprecated with no valid alternative
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = getSystemService(PowerManager.class);
        notificationManager = getSystemService(NotificationManager.class);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_LOCK_TAG);
        screenOffReceiver = new ScreenOffReceiver();

        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_TOGGLE_STAY_AWAKE.equals(action)) {
            toggleKeepingScreenOn();
        } else if (ACTION_STOP_KEEPING_SCREEN_ON.equals(action)) {
            stopKeepingScreenOn();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(screenOffReceiver);
        super.onDestroy();
    }

    @Override
    public void onStartListening() {
        refreshQsTile();
    }

    @Override
    public void onClick() {
        toggleKeepingScreenOn();
    }

    private void toggleKeepingScreenOn() {
        if (!wakeLock.isHeld()) {
            startKeepingScreenOn();
        } else {
            stopKeepingScreenOn();
        }
    }

    /**
     * Acquire a wake lock and set a foreground service notification.
     */
    @SuppressLint("WakelockTimeout")
    private void startKeepingScreenOn() {
        wakeLock.acquire();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_description),
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent stopIntent = new Intent(ACTION_STOP_KEEPING_SCREEN_ON, null, this, getClass());
        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(PendingIntent.getService(this, 0, stopIntent, 0))
                .build();
        startForeground(1, notification);
        refreshQsTile();
    }

    /**
     * Release the wake lock and remove the foreground service notification.
     */
    private void stopKeepingScreenOn() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        stopForeground(true);
        refreshQsTile();
    }

    private void refreshQsTile() {
        Tile qsTile = getQsTile();
        if (qsTile != null) {
            qsTile.setState(wakeLock.isHeld() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            qsTile.updateTile();
        }
    }

    /**
     * Disable when the screen is turned off.
     */
    private class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                stopKeepingScreenOn();
            }
        }
    }
}
