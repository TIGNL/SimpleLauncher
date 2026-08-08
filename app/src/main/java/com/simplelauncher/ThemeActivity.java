package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ThemeActivity extends Activity {

    private SharedPreferences prefs;
    private View checkFollowSystem;
    private View checkWhite;
    private View checkDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        ((TextView) findViewById(R.id.pageTitle)).setText("Theme");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        checkFollowSystem = findViewById(R.id.checkFollowSystem);
        checkWhite = findViewById(R.id.checkWhite);
        checkDark = findViewById(R.id.checkDark);

        updateUI();

        findViewById(R.id.optFollowSystem).setOnClickListener(v -> {
            prefs.edit().putInt("theme", 0).apply();
            recreate();
        });

        findViewById(R.id.optWhite).setOnClickListener(v -> {
            prefs.edit().putInt("theme", 1).apply();
            recreate();
        });

        findViewById(R.id.optDark).setOnClickListener(v -> {
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
        checkFollowSystem.setVisibility(theme == 0 ? View.VISIBLE : View.GONE);
        checkWhite.setVisibility(theme == 1 ? View.VISIBLE : View.GONE);
        checkDark.setVisibility(theme == 2 ? View.VISIBLE : View.GONE);
    }
}
