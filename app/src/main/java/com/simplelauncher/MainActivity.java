package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityY < -500) {
                    showAppList();
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                finish();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        loadApps();
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
        searchInput = appListView.findViewById(R.id.searchInput);
        listView = appListView.findViewById(R.id.appList);

        adapter = new GridAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            int pos = position * 2;
            if (pos < filtered.size()) launchApp(filtered.get(pos)[1]);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            int pos = position * 2 + 1;
            if (pos < filtered.size()) {
                launchApp(filtered.get(pos)[1]);
                return true;
            }
            return false;
        });

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
                adapter.notifyDataSetChanged();

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
        searchInput = null;
        listView = null;
    }

    private void launchApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            closeAppList();
        }
    }

    @Override
    public void onBackPressed() {
        if (appListView != null) {
            closeAppList();
        } else {
            super.onBackPressed();
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
                holder.leftContainer = convertView.findViewById(R.id.leftContainer);
                holder.leftName = convertView.findViewById(R.id.leftName);
                holder.divider = convertView.findViewById(R.id.divider);
                holder.rightContainer = convertView.findViewById(R.id.rightContainer);
                holder.rightName = convertView.findViewById(R.id.rightName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            int leftPos = position * 2;
            int rightPos = position * 2 + 1;

            if (leftPos < filtered.size()) {
                holder.leftContainer.setVisibility(View.VISIBLE);
                holder.leftName.setText(filtered.get(leftPos)[0]);
            } else {
                holder.leftContainer.setVisibility(View.INVISIBLE);
            }

            if (rightPos < filtered.size()) {
                holder.rightContainer.setVisibility(View.VISIBLE);
                holder.rightName.setText(filtered.get(rightPos)[0]);
            } else {
                holder.rightContainer.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            View leftContainer;
            TextView leftName;
            View divider;
            View rightContainer;
            TextView rightName;
        }
    }
}
