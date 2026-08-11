package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class AppDrawerSettingsActivity extends Activity {

    private SharedPreferences prefs;
    private TextView textAlignmentStatus;
    private TextView itemsPerRowStatus;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer_settings);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null && velocityY > 500 && (e2.getY() - e1.getY()) > 80) {
                    goHome();
                    return true;
                }
                return false;
            }
        });

        findViewById(android.R.id.content).setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        ((TextView) findViewById(R.id.pageTitle)).setText("App Drawer");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        textAlignmentStatus = findViewById(R.id.textAlignmentStatus);
        itemsPerRowStatus = findViewById(R.id.itemsPerRowStatus);

        updateUI();

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
    }

    private void updateUI() {
        String alignment = prefs.getString("text_alignment", "start");
        int itemsPerRow = prefs.getInt("items_per_row", 2);

        textAlignmentStatus.setText(alignment.equals("start") ? "Left" : "Centered");
        itemsPerRowStatus.setText(String.valueOf(itemsPerRow));
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
