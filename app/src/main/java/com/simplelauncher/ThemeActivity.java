package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class ThemeActivity extends Activity {

    private SharedPreferences prefs;
    private TextView optFollowSystem;
    private TextView optWhite;
    private TextView optDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        ((TextView) findViewById(R.id.pageTitle)).setText("Theme");

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
        optFollowSystem.setTypeface(null, theme == 0 ? Typeface.BOLD : Typeface.NORMAL);
        optWhite.setTypeface(null, theme == 1 ? Typeface.BOLD : Typeface.NORMAL);
        optDark.setTypeface(null, theme == 2 ? Typeface.BOLD : Typeface.NORMAL);
    }
}
