package com.github.shingyx.stayawake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class StayAwakeService extends TileService {
    public final static String ACTION_TOGGLE_STAY_AWAKE = "com.github.shingyx.stayawake.TOGGLE_STAY_AWAKE";
    public final static String ACTION_STOP_KEEPING_SCREEN_ON = "com.github.shingyx.stayawake.STOP_KEEPING_SCREEN_ON";
    public final static int NOTIFICATION_ID = 1;

    private AppPreferences appPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        appPreferences = new AppPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_TOGGLE_STAY_AWAKE.equals(action)) {
            toggleKeepingScreenOn();
        } else if (ACTION_STOP_KEEPING_SCREEN_ON.equals(action)) {
            stopKeepingScreenOn();
        } else {
            refreshQsTile();
        }

        return START_NOT_STICKY;
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
        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, R.string.prompt_allow_write_settings, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivity(intent);
            } else {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);
                startActivityAndCollapse(pendingIntent);
            }
            return;
        }

        if (!appPreferences.hasPreviousScreenTimeout()) {
            startKeepingScreenOn();
        } else {
            stopKeepingScreenOn();
        }
        refreshQsTile();
    }

    private void startKeepingScreenOn() {
        if (!appPreferences.hasPreviousScreenTimeout()) {
            int screenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MIN_VALUE);
            appPreferences.setPreviousScreenTimeout(screenTimeout);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);

            createNotification();
        }
    }

    private void stopKeepingScreenOn() {
        if (appPreferences.hasPreviousScreenTimeout()) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, appPreferences.getPreviousScreenTimeout());
            appPreferences.clearPreviousScreenTimeout();

            clearNotification();
        }
    }

    private void refreshQsTile() {
        Tile qsTile = getQsTile();
        if (qsTile != null) {
            int state = appPreferences.hasPreviousScreenTimeout()
                    ? Tile.STATE_ACTIVE
                    : Tile.STATE_INACTIVE;
            qsTile.setState(state);
            qsTile.updateTile();
        }
    }

    private void createNotification() {
        Intent stopIntent = new Intent(ACTION_STOP_KEEPING_SCREEN_ON, null, this, getClass());
        Notification notification = new Notification.Builder(this, StayAwakeApplication.SERVICE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE))
                .build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void clearNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
