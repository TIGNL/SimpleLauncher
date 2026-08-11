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
import android.view.Gravity;
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
    private Runnable pendingAutoLaunch;
    private TextView pageTitle;
    private int itemsPerRow;
    private String textAlignment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchInput = findViewById(R.id.searchInput);
        searchInput.setFocusable(false);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (appListView == null) {
                    showAppList();
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    float diffY = e2.getY() - e1.getY();
                    if (appListView == null) {
                        if (velocityY < -500) {
                            showAppList();
                            return true;
                        } else if (velocityY > 500) {
                            int screenWidth = getResources().getDisplayMetrics().widthPixels;
                            if (e1.getX() < screenWidth / 2) {
                                expandNotifications();
                            } else {
                                expandQuickSettings();
                            }
                            return true;
                        }
                    } else {
                        boolean swipeClose = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("swipe_to_close", true);
                        if (swipeClose && velocityY > 500 && diffY > 80 && listView != null
                                && listView.getFirstVisiblePosition() == 0) {
                            closeAppList();
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (appListView == null) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
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
        } else {
            closeAppList();
        }
        if (appListView == null) {
            hideKeyboard();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        closeAppList();
        if (appListView == null) {
            hideKeyboard();
        }
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

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (pendingAutoLaunch != null) {
                    listView.removeCallbacks(pendingAutoLaunch);
                    pendingAutoLaunch = null;
                }

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
                if (pageTitle != null) {
                    pageTitle.setText("Apps");
                }

                if (!query.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                    boolean autoSingle = prefs.getBoolean("auto_launch_single", true);
                    boolean autoExact = prefs.getBoolean("auto_launch_exact", true);
                    String exactPkg = null;
                    if (autoSingle && filtered.size() == 1) {
                        exactPkg = filtered.get(0)[1];
                    } else if (autoExact) {
                        for (String[] app : filtered) {
                            if (app[0].equalsIgnoreCase(s.toString().trim())) {
                                exactPkg = app[1];
                                break;
                            }
                        }
                    }
                    if (exactPkg != null) {
                        String pkg = exactPkg;
                        pendingAutoLaunch = () -> {
                            if (listView != null && appListView != null) {
                                launchApp(pkg);
                            }
                            pendingAutoLaunch = null;
                        };
                        listView.postDelayed(pendingAutoLaunch, 300);
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
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        itemsPerRow = prefs.getInt("items_per_row", 2);
        textAlignment = prefs.getString("text_alignment", "start");
    }

    private void showAppList() {
        if (appListView != null) return;

        appListView = getLayoutInflater().inflate(R.layout.activity_applist, null);
        listView = appListView.findViewById(R.id.appList);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        itemsPerRow = prefs.getInt("items_per_row", 2);
        textAlignment = prefs.getString("text_alignment", "start");
        boolean autoKeyboard = prefs.getBoolean("auto_show_keyboard", true);

        View titleView = getLayoutInflater().inflate(R.layout.item_page_title, listView, false);
        pageTitle = titleView.findViewById(R.id.pageTitle);
        pageTitle.setText("Apps");
        listView.addHeaderView(titleView);

        adapter = new GridAdapter();
        listView.setAdapter(adapter);

        FrameLayout container = findViewById(R.id.container);
        container.addView(appListView);

        searchInput.setFocusable(true);
        searchInput.setFocusableInTouchMode(true);
        searchInput.requestFocus();
        if (autoKeyboard) {
            listView.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }, 200);
        }

        listView.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(android.widget.AbsListView view, int scrollState) {
                if (appListView == null || listView == null) return;
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
                    boolean hideOnScroll = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("hide_keyboard_on_scroll", true);
                    if (hideOnScroll) {
                        hideKeyboard();
                        searchInput.clearFocus();
                    }
                }
            }

            @Override
            public void onScroll(android.widget.AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });

        searchInput.setOnClickListener(v -> {
            searchInput.setFocusable(true);
            searchInput.setFocusableInTouchMode(true);
            searchInput.requestFocus();
            listView.postDelayed(() -> {
                if (appListView == null) return;
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }, 200);
        });
    }

    private void closeAppList() {
        if (appListView == null) return;
        if (pendingAutoLaunch != null && listView != null) {
            listView.removeCallbacks(pendingAutoLaunch);
            pendingAutoLaunch = null;
        }
        if (listView != null) {
            listView.setOnScrollListener(null);
        }
        hideKeyboard();
        searchInput.setText("");
        searchInput.setFocusable(false);
        FrameLayout container = findViewById(R.id.container);
        container.removeView(appListView);
        appListView = null;
        listView = null;
        adapter = null;
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void expandNotifications() {
        LauncherAccessibilityService svc = LauncherAccessibilityService.getInstance();
        if (svc != null) {
            svc.openNotifications();
        }
    }

    private void expandQuickSettings() {
        LauncherAccessibilityService svc = LauncherAccessibilityService.getInstance();
        if (svc != null) {
            svc.openQuickSettings();
        }
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

    private int getTextGravity() {
        return textAlignment.equals("center") ? Gravity.CENTER : Gravity.START;
    }

    private class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (itemsPerRow == 1) {
                return filtered.size();
            }
            return (filtered.size() + itemsPerRow - 1) / itemsPerRow;
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
            if (itemsPerRow == 1) {
                return getSingleRow(position, convertView, parent);
            }
            return getMultiRow(position, convertView, parent);
        }

        private View getSingleRow(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.findViewById(R.id.appName) == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_app_single, parent, false);
            }
            TextView appName = convertView.findViewById(R.id.appName);
            appName.setGravity(getTextGravity() | Gravity.CENTER_VERTICAL);
            if (position < filtered.size()) {
                appName.setText(filtered.get(position)[0]);
                convertView.setOnClickListener(v -> launchApp(filtered.get(position)[1]));
                convertView.setOnLongClickListener(v -> {
                    openAppInfo(filtered.get(position)[1]);
                    return true;
                });
            }
            return convertView;
        }

        private View getMultiRow(int position, View convertView, ViewGroup parent) {
            MultiRowHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof MultiRowHolder)) {
                convertView = createMultiRow(parent);
                holder = new MultiRowHolder();
                holder.boxes = new FrameLayout[itemsPerRow];
                holder.names = new TextView[itemsPerRow];
                holder.dividers = new View[itemsPerRow - 1];
                for (int i = 0; i < itemsPerRow; i++) {
                    holder.boxes[i] = convertView.findViewById(getIdForIndex(i, "box"));
                    holder.names[i] = convertView.findViewById(getIdForIndex(i, "name"));
                    holder.names[i].setGravity(getTextGravity() | Gravity.CENTER_VERTICAL);
                    if (i < itemsPerRow - 1) {
                        holder.dividers[i] = convertView.findViewById(getIdForIndex(i, "divider"));
                    }
                }
                convertView.setTag(holder);
            } else {
                holder = (MultiRowHolder) convertView.getTag();
                for (int i = 0; i < itemsPerRow; i++) {
                    holder.names[i].setGravity(getTextGravity() | Gravity.CENTER_VERTICAL);
                }
            }

            int startIndex = position * itemsPerRow;
            for (int i = 0; i < itemsPerRow; i++) {
                int appIndex = startIndex + i;
                if (appIndex < filtered.size()) {
                    holder.boxes[i].setVisibility(View.VISIBLE);
                    holder.names[i].setText(filtered.get(appIndex)[0]);
                    final int idx = appIndex;
                    holder.boxes[i].setOnClickListener(v -> launchApp(filtered.get(idx)[1]));
                    holder.boxes[i].setOnLongClickListener(v -> {
                        openAppInfo(filtered.get(idx)[1]);
                        return true;
                    });
                } else {
                    holder.boxes[i].setVisibility(View.INVISIBLE);
                    holder.boxes[i].setOnClickListener(null);
                    holder.boxes[i].setOnLongClickListener(null);
                }
                if (i < itemsPerRow - 1) {
                    boolean showDivider = appIndex < filtered.size() && (appIndex + 1) < filtered.size();
                    holder.dividers[i].setVisibility(showDivider ? View.VISIBLE : View.GONE);
                }
            }

            return convertView;
        }

        private View createMultiRow(ViewGroup parent) {
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.row_height)));
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0; i < itemsPerRow; i++) {
                FrameLayout box = new FrameLayout(MainActivity.this);
                box.setId(getIdForIndex(i, "box"));
                LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                box.setLayoutParams(boxParams);
                box.setBackgroundResource(android.R.attr.selectableItemBackground);

                TextView name = new TextView(MainActivity.this);
                name.setId(getIdForIndex(i, "name"));
                FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.content_height));
                nameParams.gravity = Gravity.CENTER_VERTICAL;
                name.setLayoutParams(nameParams);
                name.setEllipsize(android.text.TextUtils.TruncateAt.END);
                name.setMaxLines(1);
                name.setPadding(getResources().getDimensionPixelSize(R.dimen.padding), 0, getResources().getDimensionPixelSize(R.dimen.padding), 0);
                name.setTextColor(getResources().getColor(R.color.text_primary));
                name.setTextSize(14);

                box.addView(name);
                row.addView(box);

                if (i < itemsPerRow - 1) {
                    View divider = new View(MainActivity.this);
                    divider.setId(getIdForIndex(i, "divider"));
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.divider), ViewGroup.LayoutParams.MATCH_PARENT);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundResource(R.drawable.divider_vertical);
                    row.addView(divider);
                }
            }

            return row;
        }

        private int getIdForIndex(int index, String type) {
            int base = 1000 + index * 10;
            switch (type) {
                case "box": return base;
                case "name": return base + 1;
                case "divider": return base + 2;
                default: return base;
            }
        }

        class MultiRowHolder {
            FrameLayout[] boxes;
            TextView[] names;
            View[] dividers;
        }
    }
}
