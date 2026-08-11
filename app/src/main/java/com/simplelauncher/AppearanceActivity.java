package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class AppearanceActivity extends Activity {

    private SharedPreferences prefs;
    private TextView swipeToCloseStatus;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

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

        ((TextView) findViewById(R.id.pageTitle)).setText("UI and Appearance");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        swipeToCloseStatus = findViewById(R.id.swipeToCloseStatus);

        updateUI();

        findViewById(R.id.themeRow).setOnClickListener(v -> {
            startActivity(new Intent(this, ThemeActivity.class));
        });

        findViewById(R.id.swipeToCloseRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("swipe_to_close", true);
            prefs.edit().putBoolean("swipe_to_close", !current).apply();
            updateUI();
        });

        findViewById(R.id.appDrawerRow).setOnClickListener(v -> {
            startActivity(new Intent(this, AppDrawerSettingsActivity.class));
        });

        findViewById(R.id.themeRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Theme");
            i.putExtra("description", "Controls the visual appearance of the app.");
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

        findViewById(R.id.appDrawerRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "App Drawer");
            i.putExtra("description", "Configures how apps are displayed in the app drawer.");
            startActivity(i);
            return true;
        });
    }

    private void updateUI() {
        boolean swipeClose = prefs.getBoolean("swipe_to_close", true);

        swipeToCloseStatus.setText(swipeClose ? "On" : "Off");
        swipeToCloseStatus.setTextColor(swipeClose ? 0xFF00AA00 : 0xFFCC0000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
