package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class GesturesActivity extends Activity {

    private SharedPreferences prefs;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestures);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null && velocityY > 500 && (e2.getY() - e1.getY()) > 80) {
                    finish();
                    return true;
                }
                return false;
            }
        });

        ((TextView) findViewById(R.id.pageTitle)).setText("Gestures");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        bindRow(R.id.longPressRow, R.id.longPressStatus, "gesture_long_press",
                "Long press",
                "Choose what pressing and holding the home screen does.");
        bindRow(R.id.swipeDownLeftRow, R.id.swipeDownLeftStatus, "gesture_down_left",
                "Swipe down (left)",
                "Choose what swiping down on the left half of the home screen does.");
        bindRow(R.id.swipeDownRightRow, R.id.swipeDownRightStatus, "gesture_down_right",
                "Swipe down (right)",
                "Choose what swiping down on the right half of the home screen does.");
        bindRow(R.id.swipeLeftRow, R.id.swipeLeftStatus, "gesture_left",
                "Swipe left",
                "Choose what swiping left on the home screen does.");
        bindRow(R.id.swipeRightRow, R.id.swipeRightStatus, "gesture_right",
                "Swipe right",
                "Choose what swiping right on the home screen does.");
        bindRow(R.id.swipeUpRow, R.id.swipeUpStatus, "gesture_up",
                "Swipe up",
                "Always opens the app drawer so you can reach your apps.");
        bindRow(R.id.tapRow, R.id.tapStatus, "gesture_tap",
                "Tap",
                "Choose what tapping the home screen does.");
    }

    private void bindRow(int rowId, int statusId, final String key, String title, final String description) {
        TextView status = findViewById(statusId);
        status.setText(GestureActions.labelFor(this, prefs.getString(key, GestureActions.defaultFor(key))));

        TextView row = findViewById(rowId);
        row.setOnClickListener(v -> {
            if (GestureActions.isFixed(key)) {
                String message = key.equals("gesture_up")
                        ? "Swipe up always opens the app drawer"
                        : "Long press always opens Settings";
                Toast.makeText(GesturesActivity.this, message, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(GesturesActivity.this, ActionPickerActivity.class);
            i.putExtra("gesture", key);
            i.putExtra("title", title);
            startActivity(i);
        });
        row.setOnLongClickListener(v -> {
            Intent i = new Intent(GesturesActivity.this, DescriptionActivity.class);
            i.putExtra("title", title);
            i.putExtra("description", description);
            startActivity(i);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the status labels after coming back from the picker.
        bindStatus(R.id.longPressStatus, "gesture_long_press");
        bindStatus(R.id.swipeDownLeftStatus, "gesture_down_left");
        bindStatus(R.id.swipeDownRightStatus, "gesture_down_right");
        bindStatus(R.id.swipeLeftStatus, "gesture_left");
        bindStatus(R.id.swipeRightStatus, "gesture_right");
        bindStatus(R.id.swipeUpStatus, "gesture_up");
        bindStatus(R.id.tapStatus, "gesture_tap");
    }

    private void bindStatus(int statusId, String key) {
        TextView status = findViewById(statusId);
        status.setText(GestureActions.labelFor(this, prefs.getString(key, GestureActions.defaultFor(key))));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
