package com.simplelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private GestureDetector gestureDetector;
    private View appListView;
    private EditText searchInput;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String[]> apps = new ArrayList<>();
    private List<String[]> filtered = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        });

        loadApps();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
        filtered.addAll(apps);
    }

    private void showAppList() {
        if (appListView != null) return;

        appListView = getLayoutInflater().inflate(R.layout.activity_applist, null);
        searchInput = appListView.findViewById(R.id.searchInput);
        listView = appListView.findViewById(R.id.appList);

        List<String> names = new ArrayList<>();
        for (String[] app : filtered) names.add(app[0]);
        adapter = new ArrayAdapter<>(this, R.layout.item_app, R.id.appName, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            launchApp(filtered.get(position)[1]);
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
                List<String> names = new ArrayList<>();
                for (String[] app : filtered) names.add(app[0]);
                adapter.clear();
                adapter.addAll(names);
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
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        FrameLayout container = findViewById(R.id.container);
        container.addView(appListView);

        searchInput.requestFocus();
        listView.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
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
}
