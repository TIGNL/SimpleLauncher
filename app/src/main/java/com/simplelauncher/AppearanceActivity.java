package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class AppearanceActivity extends Activity {

    private SharedPreferences prefs;
    private TextView textAlignmentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        ((TextView) findViewById(R.id.pageTitle)).setText("Appearance");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        textAlignmentStatus = findViewById(R.id.textAlignmentStatus);

        updateUI();

        findViewById(R.id.themeRow).setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });

        findViewById(R.id.textAlignmentRow).setOnClickListener(v -> {
            String current = prefs.getString("text_alignment", "start");
            prefs.edit().putString("text_alignment", current.equals("start") ? "center" : "start").apply();
            updateUI();
        });

        findViewById(R.id.themeRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Theme");
            i.putExtra("description", "Controls the visual appearance of the app.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.textAlignmentRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Text alignment");
            i.putExtra("description", "Sets how app names are aligned in the app drawer.");
            startActivity(i);
            return true;
        });
    }

    private void updateUI() {
        String alignment = prefs.getString("text_alignment", "start");
        textAlignmentStatus.setText(alignment.equals("start") ? "Left" : "Centered");
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
