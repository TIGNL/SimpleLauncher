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
    private TextView itemsPerRowStatus;
    private TextView swipeToCloseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        ((TextView) findViewById(R.id.pageTitle)).setText("UI and Appearance");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        textAlignmentStatus = findViewById(R.id.textAlignmentStatus);
        itemsPerRowStatus = findViewById(R.id.itemsPerRowStatus);
        swipeToCloseStatus = findViewById(R.id.swipeToCloseStatus);

        updateUI();

        findViewById(R.id.themeRow).setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });

        findViewById(R.id.textAlignmentRow).setOnClickListener(v -> {
            String current = prefs.getString("text_alignment", "start");
            prefs.edit().putString("text_alignment", current.equals("start") ? "center" : "start").apply();
            updateUI();
        });

        findViewById(R.id.itemsPerRowRow).setOnClickListener(v -> {
            int current = prefs.getInt("items_per_row", 2);
            prefs.edit().putInt("items_per_row", current == 1 ? 2 : 1).apply();
            updateUI();
        });

        findViewById(R.id.swipeToCloseRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("swipe_to_close", true);
            prefs.edit().putBoolean("swipe_to_close", !current).apply();
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

        findViewById(R.id.itemsPerRowRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Items per row");
            i.putExtra("description", "Sets how many apps to display per row in the app drawer.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.swipeToCloseRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Swipe down to close");
            i.putExtra("description", "Allows closing the app drawer by swiping down from the top.");
            startActivity(i);
            return true;
        });
    }

    private void updateUI() {
        String alignment = prefs.getString("text_alignment", "start");
        int itemsPerRow = prefs.getInt("items_per_row", 2);
        boolean swipeClose = prefs.getBoolean("swipe_to_close", true);

        textAlignmentStatus.setText(alignment.equals("start") ? "Left" : "Centered");
        itemsPerRowStatus.setText(String.valueOf(itemsPerRow));
        swipeToCloseStatus.setText(swipeClose ? "On" : "Off");
        swipeToCloseStatus.setTextColor(swipeClose ? 0xFF00AA00 : 0xFFCC0000);
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
