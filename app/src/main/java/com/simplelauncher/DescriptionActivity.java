package com.simplelauncher;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.TextView;

public class DescriptionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");

        ((TextView) findViewById(R.id.pageTitle)).setText(title != null ? title : "Description");
        ((TextView) findViewById(R.id.descriptionText)).setText(description != null ? description : "");
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
