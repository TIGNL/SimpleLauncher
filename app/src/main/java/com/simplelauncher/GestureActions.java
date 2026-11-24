package com.simplelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central definitions for the home screen gestures feature: default action per
 * gesture, labels for the action list, and the app shortcuts behind shortcut actions.
 */
final class GestureActions {

    static final String NONE = "none";
    static final String CHOOSE_APP = "choose_app";
    static final String CHOOSE_SHORTCUT = "choose_shortcut";
    static final String SHORTCUT_PREFIX = "shortcut:";

    private GestureActions() {}

    /** Older builds stored system intents as intent:*; those are gone, so read them as none. */
    static String normalize(String action) {
        return action != null && action.startsWith("intent:") ? NONE : action;
    }

    /** Action used when the preference has never been written. */
    static String defaultFor(String key) {
        switch (key) {
            case "gesture_tap":
            case "gesture_up":
                return "drawer";
            case "gesture_down_left":
                return "a11y:notifications";
            case "gesture_down_right":
                return "a11y:quick_settings";
            case "gesture_long_press":
                return "settings";
            default:
                return NONE;
        }
    }

    /** Swipe up and long press stay fixed so the drawer and settings stay reachable. */
    static boolean isFixed(String key) {
        return "gesture_up".equals(key) || "gesture_long_press".equals(key);
    }

    /** True when this device can run the action, so old Androids don't show dead rows. */
    static boolean isAvailable(String action) {
        if (action == null) return false;
        switch (action) {
            case "a11y:lock_screen":
            case "a11y:screenshot":
                return Build.VERSION.SDK_INT >= 28;
            case "a11y:dismiss_shade":
            case "a11y:accessibility_shortcut":
                return Build.VERSION.SDK_INT >= 31;
            default:
                return true;
        }
    }

    /** Human label shown on the gestures page and in the picker. */
    static String labelFor(Context context, String action) {
        action = normalize(action);
        if (action == null || action.isEmpty()) return "Nothing";
        switch (action) {
            case NONE: return "Nothing";
            case "drawer": return "Open app drawer";
            case "settings": return "Open Settings";

            case "a11y:accessibility_shortcut": return "Accessibility shortcut";
            case "a11y:dismiss_shade": return "Close notifications";
            case "a11y:lock_screen": return "Lock screen";
            case "a11y:notifications": return "Open notifications";
            case "a11y:power_dialog": return "Open power menu";
            case "a11y:quick_settings": return "Open quick settings";
            case "a11y:recents": return "Open recent apps";
            case "a11y:screenshot": return "Take screenshot";

            default:
                if (action.startsWith("app:")) {
                    return appLabel(context, action.substring(4));
                }
                if (action.startsWith(SHORTCUT_PREFIX)) {
                    return shortcutLabel(context, action);
                }
                return "Nothing";
        }
    }

    static String appLabel(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent i = pm.getLaunchIntentForPackage(packageName);
            if (i != null && i.getComponent() != null) {
                return pm.getActivityInfo(i.getComponent(), 0).loadLabel(pm).toString();
            }
        } catch (Exception ignored) {
        }
        return "Selected app";
    }

    /** Label of the app owning a package, falling back to the package name. */
    static String packageLabel(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (Exception ignored) {
        }
        return packageName;
    }

    static boolean isShortcut(String action) {
        return action != null && action.startsWith(SHORTCUT_PREFIX);
    }

    /** Package part of shortcut:&lt;package&gt;/&lt;id&gt;. Package names never contain '/'. */
    static String shortcutPackage(String action) {
        if (!isShortcut(action)) return null;
        String rest = action.substring(SHORTCUT_PREFIX.length());
        int slash = rest.indexOf('/');
        return slash > 0 ? rest.substring(0, slash) : null;
    }

    /** Id part of shortcut:&lt;package&gt;/&lt;id&gt;, kept whole even if it contains '/'. */
    static String shortcutId(String action) {
        if (!isShortcut(action)) return null;
        String rest = action.substring(SHORTCUT_PREFIX.length());
        int slash = rest.indexOf('/');
        return slash >= 0 && slash < rest.length() - 1 ? rest.substring(slash + 1) : null;
    }

    /** Label of a saved shortcut, or "Missing shortcut" when it no longer exists. */
    static String shortcutLabel(Context context, String action) {
        String pkg = shortcutPackage(action);
        String id = shortcutId(action);
        if (pkg == null || id == null) return "Missing shortcut";
        ShortcutInfo info = findShortcut(context, pkg, id);
        if (info == null) return "Missing shortcut";
        return shortcutLabelOf(info);
    }

    /** Label shown for one shortcut: short label, then long label, then its id. */
    static String shortcutLabelOf(ShortcutInfo info) {
        CharSequence label = info.getShortLabel();
        if (label == null || label.length() == 0) label = info.getLongLabel();
        if (label == null || label.length() == 0) label = info.getId();
        return label.toString();
    }

    private static LauncherApps launcherApps(Context context) {
        return (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    /** True when this app may read and start other apps' shortcuts (default launcher). */
    static boolean hasShortcutHostPermission(Context context) {
        try {
            LauncherApps la = launcherApps(context);
            return la != null && la.hasShortcutHostPermission();
        } catch (Exception e) {
            return false;
        }
    }

    /** Every enabled shortcut on the device, excluding our own package. */
    static List<ShortcutInfo> allShortcuts(Context context) {
        List<ShortcutInfo> result = new ArrayList<>();
        LauncherApps la = launcherApps(context);
        if (la == null || !la.hasShortcutHostPermission()) return result;
        try {
            LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
            query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    | LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                    | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
            List<ShortcutInfo> shortcuts = la.getShortcuts(query, Process.myUserHandle());
            if (shortcuts != null) {
                for (ShortcutInfo info : shortcuts) {
                    if (info.isEnabled() && !context.getPackageName().equals(info.getPackage())) {
                        result.add(info);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /** One saved shortcut resolved from the device, or null when it is gone. */
    static ShortcutInfo findShortcut(Context context, String packageName, String id) {
        if (packageName == null || id == null) return null;
        LauncherApps la = launcherApps(context);
        if (la == null || !la.hasShortcutHostPermission()) return null;
        try {
            LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
            query.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    | LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                    | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED);
            query.setPackage(packageName);
            query.setShortcutIds(Collections.singletonList(id));
            List<ShortcutInfo> shortcuts = la.getShortcuts(query, Process.myUserHandle());
            if (shortcuts != null) {
                for (ShortcutInfo info : shortcuts) {
                    if (id.equals(info.getId())) return info;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Apps publishing at least one enabled shortcut, ordered A-Z by label. */
    static List<String[]> appsWithShortcuts(Context context) {
        List<String[]> apps = new ArrayList<>();
        if (!hasShortcutHostPermission(context)) return apps;
        List<String> seen = new ArrayList<>();
        for (ShortcutInfo info : allShortcuts(context)) {
            String pkg = info.getPackage();
            if (pkg != null && !seen.contains(pkg)) {
                seen.add(pkg);
                apps.add(new String[]{packageLabel(context, pkg), pkg});
            }
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        return apps;
    }

    /** Enabled shortcuts of one app, ordered A-Z by label. */
    static List<ShortcutInfo> shortcutsOf(Context context, String packageName) {
        List<ShortcutInfo> result = new ArrayList<>();
        for (ShortcutInfo info : allShortcuts(context)) {
            if (packageName != null && packageName.equals(info.getPackage())) {
                result.add(info);
            }
        }
        Collections.sort(result, (a, b) ->
                shortcutLabelOf(a).compareToIgnoreCase(shortcutLabelOf(b)));
        return result;
    }

    /** The accessibility actions this build offers, already ordered A-Z. */
    static String[][] accessibilityActions() {
        return new String[][]{
                {"a11y:accessibility_shortcut", "Accessibility shortcut"},
                {"a11y:dismiss_shade", "Close notifications"},
                {"a11y:lock_screen", "Lock screen"},
                {"a11y:notifications", "Open notifications"},
                {"a11y:power_dialog", "Open power menu"},
                {"a11y:quick_settings", "Open quick settings"},
                {"a11y:recents", "Open recent apps"},
                {"a11y:screenshot", "Take screenshot"},
        };
    }
}
