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

        TextView appearanceRow = findViewById(R.id.appearanceRow);
        appearanceRow.setOnClickListener(v -> {
            startActivity(new Intent(this, AppearanceActivity.class));
        });
        appearanceRow.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "UI and Appearance");
            i.putExtra("description", "Controls the visual appearance and UI behavior of the app.");
            startActivity(i);
            return true;
        });

        TextView searchRow = findViewById(R.id.searchRow);
        searchRow.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
        searchRow.setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Search");
            i.putExtra("description", "Configures search behavior and keyboard settings.");
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
            i.putExtra("description", "Manages permissions required for core functionality.");
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
