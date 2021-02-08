package com.github.shingyx.stayawake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ShortcutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            createShortcut();
        } else if (StayAwakeService.ACTION_TOGGLE_STAY_AWAKE.equals(action)) {
            toggleStayAwake();
        }

        finish();
    }

    @SuppressWarnings("deprecation") // Use deprecated approach for no icon badge
    private void createShortcut() {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(StayAwakeService.ACTION_TOGGLE_STAY_AWAKE, null, this, getClass()));
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));
        setResult(Activity.RESULT_OK, intent);
    }

    private void toggleStayAwake() {
        Intent intent = new Intent(StayAwakeService.ACTION_TOGGLE_STAY_AWAKE, null, this, StayAwakeService.class);
        startService(intent);
    }
}
