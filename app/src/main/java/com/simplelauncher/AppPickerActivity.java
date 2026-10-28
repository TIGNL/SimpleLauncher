package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Lists every installed app so a gesture can launch one of them. */
public class AppPickerActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout container;
    private String gestureKey;
    private GestureDetector gestureDetector;

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
                    finish();
                    return true;
                }
                return false;
            }
        });

        gestureKey = getIntent().getStringExtra("gesture");
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        ((TextView) findViewById(R.id.pageTitle)).setText("Applications");

        container = findViewById(R.id.appContainer);
        build();
    }

    private void build() {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        java.util.List<ResolveInfo> installed = getPackageManager().queryIntentActivities(query, 0);

        java.util.List<String[]> apps = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ResolveInfo info : installed) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            apps.add(new String[]{info.loadLabel(getPackageManager()).toString(), pkg});
        }
        java.util.Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));

        for (String[] app : apps) {
            addRow(app[0], app[1]);
        }
        addDivider();
    }

    private void addRow(String label, final String packageName) {
        View row = getLayoutInflater().inflate(R.layout.item_row, container, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        row.setOnClickListener(v -> {
            if (gestureKey != null) {
                prefs.edit().putString(gestureKey, "app:" + packageName).apply();
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
