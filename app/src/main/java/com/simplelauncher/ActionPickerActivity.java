package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Picks the action for one gesture. Actions are grouped into None,
 * Accessibility actions, Applications, Launcher actions and Shortcuts.
 */
public class ActionPickerActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout container;
    private String gestureKey;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_picker);

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

        gestureKey = getIntent().getStringExtra("gesture");
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        String title = getIntent().getStringExtra("title");
        ((TextView) findViewById(R.id.pageTitle)).setText(title != null ? title : "Gestures");

        container = findViewById(R.id.actionContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    private String current() {
        if (gestureKey == null) return GestureActions.NONE;
        return prefs.getString(gestureKey, GestureActions.defaultFor(gestureKey));
    }

    private void build() {
        container.removeAllViews();
        String current = current();

        addRow("Nothing", GestureActions.NONE, current);
        addDivider();

        addHeader("Accessibility actions");
        for (String[] action : GestureActions.accessibilityActions()) {
            if (GestureActions.isAvailable(action[0])) {
                addRow(action[1], action[0], current);
            }
        }

        addHeader("Applications");
        if (current.startsWith("app:")) {
            addRow(GestureActions.labelFor(this, current), current, current);
        }
        addRow("Choose an app\u2026", GestureActions.CHOOSE_APP, current);
        addDivider();

        addHeader("Launcher actions");
        addRow("Open app drawer", "drawer", current);
        addRow("Open Settings", "settings", current);
        addDivider();

        addHeader("Shortcuts");
        for (String[] action : GestureActions.shortcutActions()) {
            if (GestureActions.isAvailable(action[0])) {
                addRow(action[1], action[0], current);
            }
        }
        addDivider();
    }

    private void addHeader(String text) {
        TextView header = (TextView) getLayoutInflater().inflate(R.layout.item_section_header, container, false);
        header.setText(text);
        container.addView(header);
        addDivider();
    }

    private void addRow(String label, final String value, String current) {
        View row = getLayoutInflater().inflate(R.layout.item_row, container, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        if (value.equals(current)) {
            row.setBackgroundColor(Color.parseColor("#80808080"));
        }
        row.setOnClickListener(v -> {
            if (GestureActions.CHOOSE_APP.equals(value)) {
                Intent i = new Intent(ActionPickerActivity.this, AppPickerActivity.class);
                i.putExtra("gesture", gestureKey);
                startActivity(i);
                return;
            }
            if (gestureKey != null) {
                prefs.edit().putString(gestureKey, value).apply();
            }
            finish();
        });
        container.addView(row);
        addDivider();
    }

    private void addDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.divider));
        divider.setLayoutParams(params);
        divider.setBackgroundResource(R.drawable.divider_horizontal);
        container.addView(divider);
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
