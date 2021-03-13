package com.github.shingyx.stayawake;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class StayAwakeService extends TileService {
    public final static String ACTION_TOGGLE_STAY_AWAKE = "com.github.shingyx.stayawake.TOGGLE_STAY_AWAKE";
    public final static String ACTION_STOP_KEEPING_SCREEN_ON = "com.github.shingyx.stayawake.STOP_KEEPING_SCREEN_ON";

    private ScreenOffReceiver screenOffReceiver;
    private AppPreferences appPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        screenOffReceiver = new ScreenOffReceiver();
        appPreferences = new AppPreferences(this);

        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
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
            updateForegroundService();
        }

        return START_STICKY;
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
        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, R.string.prompt_allow_write_settings, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        if (!appPreferences.hasPreviousScreenTimeout()) {
            startKeepingScreenOn();
        } else {
            stopKeepingScreenOn();
        }
    }

    private void startKeepingScreenOn() {
        if (!appPreferences.hasPreviousScreenTimeout()) {
            int screenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MIN_VALUE);
            appPreferences.setPreviousScreenTimeout(screenTimeout);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
        }

        refreshQsTile();
        updateForegroundService();
    }

    private void stopKeepingScreenOn() {
        if (appPreferences.hasPreviousScreenTimeout()) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, appPreferences.getPreviousScreenTimeout());
            appPreferences.clearPreviousScreenTimeout();
        }

        refreshQsTile();
        updateForegroundService();
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

    private void updateForegroundService() {
        if (appPreferences.hasPreviousScreenTimeout()) {
            Intent stopIntent = new Intent(ACTION_STOP_KEEPING_SCREEN_ON, null, this, getClass());
            Notification notification = new Notification.Builder(this, StayAwakeApplication.SERVICE_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_content))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentIntent(PendingIntent.getService(this, 0, stopIntent, 0))
                    .build();
            startForeground(1, notification);
        } else {
            stopForeground(true);
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
