package com.github.shingyx.stayawake;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class StayAwakeService extends TileService {
    public final static String ACTION_TOGGLE_STAY_AWAKE = "com.github.shingyx.stayawake.TOGGLE_STAY_AWAKE";

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
        } else {
            refreshQsTile();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
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
    }

    private void stopKeepingScreenOn() {
        if (appPreferences.hasPreviousScreenTimeout()) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, appPreferences.getPreviousScreenTimeout());
            appPreferences.clearPreviousScreenTimeout();
        }

        refreshQsTile();
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
}
