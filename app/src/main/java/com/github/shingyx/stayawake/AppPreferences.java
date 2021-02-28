package com.github.shingyx.stayawake;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String NAME = "StayAwakePreferences";
    private static final String PREVIOUS_SCREEN_TIMEOUT = "PreviousScreenTimeout";
    private static final int SCREEN_TIMEOUT_UNKNOWN = Integer.MIN_VALUE;

    private final SharedPreferences sharedPreferences;

    public AppPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public int getPreviousScreenTimeout() {
        return sharedPreferences.getInt(PREVIOUS_SCREEN_TIMEOUT, SCREEN_TIMEOUT_UNKNOWN);
    }

    public void setPreviousScreenTimeout(int value) {
        sharedPreferences.edit()
                .putInt(PREVIOUS_SCREEN_TIMEOUT, value)
                .apply();
    }

    public boolean isPreviousScreenTimeoutUnknown() {
        return getPreviousScreenTimeout() == SCREEN_TIMEOUT_UNKNOWN;
    }

    public void setPreviousScreenTimeoutUnknown() {
        setPreviousScreenTimeout(SCREEN_TIMEOUT_UNKNOWN);
    }
}
