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
import android.widget.Toast;

import java.util.List;

/**
 * Lets a gesture pick a classic launcher shortcut (android.intent.action.CREATE_SHORTCUT).
 * The list shows every app that can create one, the same list Nova Launcher shows
 * under its gesture Shortcuts tab. Picking an entry runs that app's shortcut screen
 * (Google Maps for example opens its "Create widget" flow), and whatever it returns
 * is saved on the gesture.
 */
public class ShortcutPickerActivity extends Activity {

    private static final int REQUEST_CREATE_SHORTCUT = 1001;

    private SharedPreferences prefs;
    private LinearLayout container;
    private String gestureKey;
    private GestureDetector gestureDetector;
    private TextView pageTitle;
    private String pendingLabel;

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

        pageTitle = findViewById(R.id.pageTitle);
        container = findViewById(R.id.appContainer);
        build();
    }

    private void build() {
        container.removeAllViews();
        pageTitle.setText("Shortcuts");

        List<ResolveInfo> providers = GestureActions.shortcutProviders(this);
        if (providers.isEmpty()) {
            addNote("No shortcuts found on this device.");
            addDivider();
            return;
        }
        for (ResolveInfo info : providers) {
            addProviderRow(GestureActions.providerLabel(this, info), info);
        }
        addDivider();
    }

    /** One line of explanation instead of a list, e.g. when nothing can create shortcuts. */
    private void addNote(String text) {
        TextView note = (TextView) getLayoutInflater().inflate(R.layout.item_section_header, container, false);
        note.setText(text);
        container.addView(note);
    }

    private void addProviderRow(String label, final ResolveInfo info) {
        View row = getLayoutInflater().inflate(R.layout.item_row, container, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        row.setOnClickListener(v -> launchProvider(label, info));
        container.addView(row);
        addDivider();
    }

    /** Runs the app's own shortcut screen; its result comes back to onActivityResult. */
    private void launchProvider(String label, ResolveInfo info) {
        pendingLabel = label;
        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        try {
            startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
        } catch (Exception e) {
            pendingLabel = null;
            Toast.makeText(this, "This shortcut could not be opened",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CREATE_SHORTCUT) return;

        String label = pendingLabel;
        pendingLabel = null;

        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        Intent target = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (target == null) {
            Toast.makeText(this, "This app did not return a shortcut",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        if (name == null || name.isEmpty()) name = label;

        String action = GestureActions.shortcutAction(target, name);
        if (action != null && gestureKey != null) {
            prefs.edit().putString(gestureKey, action).apply();
        }
        finish();
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
