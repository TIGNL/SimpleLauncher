package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private GestureDetector gestureDetector;
    private View appListView;
    private EditText searchInput;
    private ListView listView;
    private GridAdapter adapter;
    private List<String[]> apps = new ArrayList<>();
    private List<String[]> filtered = new ArrayList<>();
    private int lastTheme = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.searchInput);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (appListView == null && velocityY < -500) {
                    showAppList();
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (appListView == null) {
                    finish();
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            }
        });

        findViewById(R.id.container).setOnClickListener(v -> {
            if (appListView == null) {
                showAppList();
            }
        });

        setupSearch();
        loadApps();
        lastTheme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentTheme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        if (lastTheme != currentTheme) {
            recreate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private void applyTheme() {
        int theme = getSharedPreferences("settings", MODE_PRIVATE).getInt("theme", 0);
        if (theme == 2) {
            setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
        } else {
            setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
        Configuration config = new Configuration(getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (theme == 2 ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();
                filtered.clear();
                if (query.isEmpty()) {
                    filtered.addAll(apps);
                } else {
                    for (String[] app : apps) {
                        if (app[0].toLowerCase().contains(query) || app[1].toLowerCase().contains(query)) {
                            filtered.add(app);
                        }
                    }
                }
                if (adapter != null) adapter.notifyDataSetChanged();

                if (!query.isEmpty() && filtered.size() == 1) {
                    listView.postDelayed(() -> launchApp(filtered.get(0)[1]), 300);
                } else if (!query.isEmpty()) {
                    for (String[] app : filtered) {
                        if (app[0].equalsIgnoreCase(s.toString().trim())) {
                            listView.postDelayed(() -> launchApp(app[1]), 300);
                            break;
                        }
                    }
                }
            }
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void loadApps() {
        apps.clear();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : list) {
            if (!info.activityInfo.packageName.equals(getPackageName())) {
                String label = info.loadLabel(getPackageManager()).toString();
                apps.add(new String[]{label, info.activityInfo.packageName});
            }
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        filtered.clear();
        filtered.addAll(apps);
    }

    private void showAppList() {
        if (appListView != null) return;

        appListView = getLayoutInflater().inflate(R.layout.activity_applist, null);
        listView = appListView.findViewById(R.id.appList);

        View titleView = getLayoutInflater().inflate(R.layout.item_page_title, listView, false);
        ((TextView) titleView.findViewById(R.id.pageTitle)).setText("Apps");
        listView.addHeaderView(titleView);

        adapter = new GridAdapter();
        listView.setAdapter(adapter);

        FrameLayout container = findViewById(R.id.container);
        container.addView(appListView);

        searchInput.requestFocus();
        listView.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void closeAppList() {
        if (appListView == null) return;
        FrameLayout container = findViewById(R.id.container);
        container.removeView(appListView);
        appListView = null;
        listView = null;
        adapter = null;
    }

    private void launchApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            closeAppList();
            searchInput.setText("");
        }
    }

    private void openAppInfo(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (appListView != null) {
            closeAppList();
            searchInput.setText("");
        }
    }

    private class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return (filtered.size() + 1) / 2;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_app_row, parent, false);
                holder = new ViewHolder();
                holder.leftBox = convertView.findViewById(R.id.leftBox);
                holder.leftName = convertView.findViewById(R.id.leftName);
                holder.divider = convertView.findViewById(R.id.divider);
                holder.rightBox = convertView.findViewById(R.id.rightBox);
                holder.rightName = convertView.findViewById(R.id.rightName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            int leftPos = position * 2;
            int rightPos = position * 2 + 1;

            if (leftPos < filtered.size()) {
                holder.leftBox.setVisibility(View.VISIBLE);
                holder.leftName.setText(filtered.get(leftPos)[0]);
                holder.leftBox.setOnClickListener(v -> launchApp(filtered.get(leftPos)[1]));
                holder.leftBox.setOnLongClickListener(v -> {
                    openAppInfo(filtered.get(leftPos)[1]);
                    return true;
                });
            } else {
                holder.leftBox.setVisibility(View.INVISIBLE);
                holder.leftBox.setOnClickListener(null);
                holder.leftBox.setOnLongClickListener(null);
            }

            if (rightPos < filtered.size()) {
                holder.divider.setVisibility(View.VISIBLE);
                holder.rightBox.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams leftParams = holder.leftBox.getLayoutParams();
                leftParams.width = 0;
                ((LinearLayout.LayoutParams) leftParams).weight = 1;
                holder.rightName.setText(filtered.get(rightPos)[0]);
                holder.rightBox.setOnClickListener(v -> launchApp(filtered.get(rightPos)[1]));
                holder.rightBox.setOnLongClickListener(v -> {
                    openAppInfo(filtered.get(rightPos)[1]);
                    return true;
                });
            } else {
                holder.divider.setVisibility(View.GONE);
                holder.rightBox.setVisibility(View.GONE);
                ViewGroup.LayoutParams leftParams = holder.leftBox.getLayoutParams();
                leftParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                ((LinearLayout.LayoutParams) leftParams).weight = 0;
                holder.rightBox.setOnClickListener(null);
                holder.rightBox.setOnLongClickListener(null);
            }

            return convertView;
        }

        class ViewHolder {
            View leftBox;
            TextView leftName;
            View divider;
            View rightBox;
            TextView rightName;
        }
    }
}
