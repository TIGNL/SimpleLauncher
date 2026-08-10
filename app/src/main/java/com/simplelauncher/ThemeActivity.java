package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ThemeActivity extends Activity {

    private SharedPreferences prefs;
    private View optFollowSystem;
    private View optWhite;
    private View optDark;

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

        optFollowSystem.setOnClickListener(v -> selectTheme(0));
        optWhite.setOnClickListener(v -> selectTheme(1));
        optDark.setOnClickListener(v -> selectTheme(2));

        optFollowSystem.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Follow system");
            i.putExtra("description", "Automatically matches your device's system-wide appearance setting. When you change your device between light and dark mode, the app will update to match.");
            startActivity(i);
            return true;
        });

        optWhite.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "White");
            i.putExtra("description", "Always uses a light appearance with a white background, regardless of your device's system-wide appearance setting.");
            startActivity(i);
            return true;
        });

        optDark.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Dark");
            i.putExtra("description", "Always uses a dark appearance with a black background, regardless of your device's system-wide appearance setting.");
            startActivity(i);
            return true;
        });
    }

    private void selectTheme(int theme) {
        int current = prefs.getInt("theme", 0);
        if (current == theme) return;
        int systemNight = getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        boolean currentDark = current == 2 || (current == 0 && systemNight == Configuration.UI_MODE_NIGHT_YES);
        boolean newDark = theme == 2 || (theme == 0 && systemNight == Configuration.UI_MODE_NIGHT_YES);
        prefs.edit().putInt("theme", theme).apply();
        if (currentDark != newDark) {
            recreate();
        } else {
            updateUI();
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

    private void updateUI() {
        int theme = prefs.getInt("theme", 0);
        int selected = Color.parseColor("#80808080");
        int unselected = Color.TRANSPARENT;
        optFollowSystem.setBackgroundColor(theme == 0 ? selected : unselected);
        optWhite.setBackgroundColor(theme == 1 ? selected : unselected);
        optDark.setBackgroundColor(theme == 2 ? selected : unselected);
    }
}
