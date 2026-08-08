package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private SharedPreferences prefs;
    private TextView optFollowSystem;
    private TextView optWhite;
    private TextView optDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((TextView) findViewById(R.id.pageTitle)).setText("Settings");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        optFollowSystem = findViewById(R.id.optFollowSystem);
        optWhite = findViewById(R.id.optWhite);
        optDark = findViewById(R.id.optDark);

        updateUI();

        optFollowSystem.setOnClickListener(v -> {
            prefs.edit().putInt("theme", 0).apply();
            recreate();
        });

        optWhite.setOnClickListener(v -> {
            prefs.edit().putInt("theme", 1).apply();
            recreate();
        });

        optDark.setOnClickListener(v -> {
            prefs.edit().putInt("theme", 2).apply();
            recreate();
        });
    }

    private void applyTheme() {
        int theme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        if (theme == 2) {
            setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        } else {
            setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
        Configuration config = new Configuration(getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (theme == 2 ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void updateUI() {
        int theme = prefs.getInt("theme", 0);
        optFollowSystem.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                theme == 0 ? android.R.drawable.ic_menu_add : 0, 0);
        optWhite.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                theme == 1 ? android.R.drawable.ic_menu_add : 0, 0);
        optDark.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                theme == 2 ? android.R.drawable.ic_menu_add : 0, 0);
    }
}
