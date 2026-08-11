package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class AppDrawerActivity extends Activity {

    private SharedPreferences prefs;
    private TextView itemsPerRowStatus;
    private TextView autoLaunchSingleStatus;
    private TextView autoLaunchExactStatus;
    private TextView autoShowKeyboardStatus;
    private TextView hideKeyboardOnScrollStatus;
    private TextView swipeToCloseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);

        ((TextView) findViewById(R.id.pageTitle)).setText("App Drawer and Search");

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        itemsPerRowStatus = findViewById(R.id.itemsPerRowStatus);
        autoLaunchSingleStatus = findViewById(R.id.autoLaunchSingleStatus);
        autoLaunchExactStatus = findViewById(R.id.autoLaunchExactStatus);
        autoShowKeyboardStatus = findViewById(R.id.autoShowKeyboardStatus);
        hideKeyboardOnScrollStatus = findViewById(R.id.hideKeyboardOnScrollStatus);
        swipeToCloseStatus = findViewById(R.id.swipeToCloseStatus);

        updateUI();

        findViewById(R.id.itemsPerRowRow).setOnClickListener(v -> {
            int current = prefs.getInt("items_per_row", 2);
            int next = current >= 4 ? 1 : current + 1;
            prefs.edit().putInt("items_per_row", next).apply();
            updateUI();
        });

        findViewById(R.id.autoLaunchSingleRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_launch_single", true);
            prefs.edit().putBoolean("auto_launch_single", !current).apply();
            updateUI();
        });

        findViewById(R.id.autoLaunchExactRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_launch_exact", true);
            prefs.edit().putBoolean("auto_launch_exact", !current).apply();
            updateUI();
        });

        findViewById(R.id.autoShowKeyboardRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_show_keyboard", true);
            prefs.edit().putBoolean("auto_show_keyboard", !current).apply();
            updateUI();
        });

        findViewById(R.id.hideKeyboardOnScrollRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("hide_keyboard_on_scroll", true);
            prefs.edit().putBoolean("hide_keyboard_on_scroll", !current).apply();
            updateUI();
        });

        findViewById(R.id.swipeToCloseRow).setOnClickListener(v -> {
            boolean current = prefs.getBoolean("swipe_to_close", true);
            prefs.edit().putBoolean("swipe_to_close", !current).apply();
            updateUI();
        });

        findViewById(R.id.itemsPerRowRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Items per row");
            i.putExtra("description", "Sets how many apps to display per row in the app drawer.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.autoLaunchSingleRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Auto-launch single match");
            i.putExtra("description", "Opens an app automatically when it is the only search result.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.autoLaunchExactRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Auto-launch exact name");
            i.putExtra("description", "Opens an app automatically when the search text matches its name exactly.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.autoShowKeyboardRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Auto-show keyboard");
            i.putExtra("description", "Automatically shows the keyboard when the app drawer opens.");
            startActivity(i);
            return true;
        });

        findViewById(R.id.hideKeyboardOnScrollRow).setOnLongClickListener(v -> {
            Intent i = new Intent(this, DescriptionActivity.class);
            i.putExtra("title", "Hide keyboard on scroll");
            i.putExtra("description", "Hides the keyboard when scrolling through the app list.");
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
        int itemsPerRow = prefs.getInt("items_per_row", 2);
        boolean single = prefs.getBoolean("auto_launch_single", true);
        boolean exact = prefs.getBoolean("auto_launch_exact", true);
        boolean autoKeyboard = prefs.getBoolean("auto_show_keyboard", true);
        boolean hideOnScroll = prefs.getBoolean("hide_keyboard_on_scroll", true);
        boolean swipeClose = prefs.getBoolean("swipe_to_close", true);

        itemsPerRowStatus.setText(String.valueOf(itemsPerRow));
        autoLaunchSingleStatus.setText(single ? "On" : "Off");
        autoLaunchSingleStatus.setTextColor(single ? 0xFF00AA00 : 0xFFCC0000);
        autoLaunchExactStatus.setText(exact ? "On" : "Off");
        autoLaunchExactStatus.setTextColor(exact ? 0xFF00AA00 : 0xFFCC0000);
        autoShowKeyboardStatus.setText(autoKeyboard ? "On" : "Off");
        autoShowKeyboardStatus.setTextColor(autoKeyboard ? 0xFF00AA00 : 0xFFCC0000);
        hideKeyboardOnScrollStatus.setText(hideOnScroll ? "On" : "Off");
        hideKeyboardOnScrollStatus.setTextColor(hideOnScroll ? 0xFF00AA00 : 0xFFCC0000);
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
