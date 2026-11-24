package com.simplelauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Lets a gesture pick a real app shortcut. First level lists the apps that
 * publish shortcuts; picking one replaces the list with that app's shortcuts,
 * and picking one of those saves shortcut:&lt;package&gt;/&lt;id&gt; on the gesture.
 * Swipe down or Back steps back a level, then closes.
 */
public class ShortcutPickerActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout container;
    private String gestureKey;
    private GestureDetector gestureDetector;
    private String selectedPackage;
    private TextView pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null && velocityY > 500 && (e2.getY() - e1.getY()) > 80) {
                    goBack();
                    return true;
                }
                return false;
            }
        });

        gestureKey = getIntent().getStringExtra("gesture");
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        pageTitle = findViewById(R.id.pageTitle);
        container = findViewById(R.id.appContainer);
        build();
    }

    /** Steps back from an app's shortcuts to the app list, then closes. */
    private void goBack() {
        if (selectedPackage != null) {
            selectedPackage = null;
            build();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (selectedPackage != null) {
            selectedPackage = null;
            build();
        } else {
            super.onBackPressed();
        }
    }

    private void build() {
        container.removeAllViews();
        if (selectedPackage == null) {
            buildAppList();
        } else {
            buildShortcutList();
        }
    }

    private void buildAppList() {
        pageTitle.setText("Shortcuts");
        if (!GestureActions.hasShortcutHostPermission(this)) {
            addNote("Set SimpleLauncher as your default home app to use shortcuts.");
            addDivider();
            return;
        }
        java.util.List<String[]> apps = GestureActions.appsWithShortcuts(this);
        if (apps.isEmpty()) {
            addNote("No shortcuts found on this device.");
            addDivider();
            return;
        }
        for (String[] app : apps) {
            addAppRow(app[0], app[1]);
        }
        addDivider();
    }

    private void buildShortcutList() {
        pageTitle.setText(GestureActions.packageLabel(this, selectedPackage));
        java.util.List<ShortcutInfo> shortcuts = GestureActions.shortcutsOf(this, selectedPackage);
        if (shortcuts.isEmpty()) {
            addNote("This app has no shortcuts right now.");
            addDivider();
            return;
        }
        for (ShortcutInfo info : shortcuts) {
            addShortcutRow(GestureActions.shortcutLabelOf(info), info.getId());
        }
        addDivider();
    }

    /** One line of explanation instead of a list, e.g. when no shortcut host exists. */
    private void addNote(String text) {
        TextView note = (TextView) getLayoutInflater().inflate(R.layout.item_section_header, container, false);
        note.setText(text);
        container.addView(note);
    }

    private void addAppRow(String label, final String packageName) {
        View row = getLayoutInflater().inflate(R.layout.item_row, container, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        row.setOnClickListener(v -> {
            selectedPackage = packageName;
            build();
        });
        container.addView(row);
        addDivider();
    }

    private void addShortcutRow(String label, final String shortcutId) {
        View row = getLayoutInflater().inflate(R.layout.item_row, container, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        row.setOnClickListener(v -> {
            if (gestureKey != null) {
                prefs.edit().putString(gestureKey,
                        GestureActions.SHORTCUT_PREFIX + selectedPackage + "/" + shortcutId).apply();
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
