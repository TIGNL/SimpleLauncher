package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class AppDrawerActivity extends Activity {

    private SharedPreferences prefs;
    private TextView autoLaunchSingleStatus;
    private TextView autoLaunchExactStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);

        ((TextView) findViewById(R.id.pageTitle)).setText("App Drawer");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        autoLaunchSingleStatus = findViewById(R.id.autoLaunchSingleStatus);
        autoLaunchExactStatus = findViewById(R.id.autoLaunchExactStatus);

        updateUI();

        findViewById(R.id.autoLaunchSingleRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_launch_single", true);
            prefs.edit().putBoolean("auto_launch_single", !current).apply();
            updateUI();
        });

        findViewById(R.id.autoLaunchExactRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_launch_exact", true);
            prefs.edit().putBoolean("auto_launch_exact", !current).apply();
            updateUI();
        });
    }

    private void updateUI() {
        boolean single = prefs.getBoolean("auto_launch_single", true);
        boolean exact = prefs.getBoolean("auto_launch_exact", true);
        autoLaunchSingleStatus.setText(single ? "On" : "Off");
        autoLaunchSingleStatus.setTextColor(single ? 0xFF00AA00 : 0xFFCC0000);
        autoLaunchExactStatus.setText(exact ? "On" : "Off");
        autoLaunchExactStatus.setTextColor(exact ? 0xFF00AA00 : 0xFFCC0000);
    }

    private void applyTheme() {
        int theme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        int systemNight = getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        int nightMode;
        if (theme == 2) {
            setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
            nightMode = Configuration.UI_MODE_NIGHT_YES;
        } else if (theme == 1) {
            setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            nightMode = Configuration.UI_MODE_NIGHT_NO;
        } else {
            if (systemNight == Configuration.UI_MODE_NIGHT_YES) {
                setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            }
            nightMode = systemNight;
        }
        Configuration config = new Configuration(getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightMode;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
