package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private int lastTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((TextView) findViewById(R.id.pageTitle)).setText("Settings");

        TextView themeRow = findViewById(R.id.themeRow);
        themeRow.setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });
        themeRow.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Theme");
            i.putExtra("description", "Choose between Dark, Light, or Follow System theme. Follow System automatically matches your device's system-wide appearance setting.");
            startActivity(i);
            return true;
        });

        TextView permissionsRow = findViewById(R.id.permissionsRow);
        permissionsRow.setOnClickListener(v -> {
            startActivity(new Intent(this, PermissionsActivity.class));
        });
        permissionsRow.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Needed Permissions");
            i.putExtra("description", "This app uses an Accessibility Service to provide quick access to notifications and quick settings via swipe-down gestures on the home screen. The service does not collect or share any data. You can enable or disable it from this page.");
            startActivity(i);
            return true;
        });

        TextView appDrawerRow = findViewById(R.id.appDrawerRow);
        appDrawerRow.setOnClickListener(v -> {
            startActivity(new Intent(this, AppDrawerActivity.class));
        });
        appDrawerRow.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "App Drawer");
            i.putExtra("description", "Customize app drawer behavior. Toggle auto-launch settings: automatically open an app when it's the only match or when the typed name exactly matches an app name.");
            startActivity(i);
            return true;
        });

        lastTheme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentTheme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        if (lastTheme != currentTheme) {
            recreate();
        }
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
