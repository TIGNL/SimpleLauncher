package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
        applyBackgroundColor();

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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void applyTheme() {
        int theme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        if (theme == 2) {
            setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        } else {
            setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
    }

    private void applyBackgroundColor() {
        int theme = prefs != null ? prefs.getInt("theme", 0) : 0;
        int color = theme == 2 ? Color.parseColor("#121212") : Color.parseColor("#F5F5F5");
        findViewById(android.R.id.content).setBackgroundColor(color);
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
