package com.simplelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central definitions for the home screen gestures feature: default action per
 * gesture, labels for the action list, and the launcher shortcuts behind
 * shortcut actions. Shortcuts are the classic android.intent.action.CREATE_SHORTCUT
 * kind (the same list Nova Launcher shows in its gesture Shortcuts tab), stored
 * as "legacy:&lt;intent uri&gt;\n&lt;label&gt;".
 */
final class GestureActions {

    static final String NONE = "none";
    static final String CHOOSE_APP = "choose_app";
    static final String CHOOSE_SHORTCUT = "choose_shortcut";
    static final String LEGACY_PREFIX = "legacy:";

    private GestureActions() {}

    /** Older builds stored system intents or app-shortcut ids; both are gone, read them as none. */
    static String normalize(String action) {
        if (action == null) return action;
        if (action.startsWith("intent:") || action.startsWith("shortcut:")) return NONE;
        return action;
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
                if (action.startsWith(LEGACY_PREFIX)) {
                    return shortcutLabel(action);
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
        return action != null && action.startsWith(LEGACY_PREFIX);
    }

    /** Saved label of a shortcut action, or "Shortcut" when it was stored without one. */
    static String shortcutLabel(String action) {
        if (!isShortcut(action)) return "Shortcut";
        int nl = action.indexOf('\n', LEGACY_PREFIX.length());
        if (nl >= 0 && nl < action.length() - 1) {
            return action.substring(nl + 1);
        }
        return "Shortcut";
    }

    /** Rebuilds the intent a saved shortcut action points at, or null when unparsable. */
    static Intent shortcutIntent(String action) {
        if (!isShortcut(action)) return null;
        int end = action.indexOf('\n');
        String uri = end >= 0
                ? action.substring(LEGACY_PREFIX.length(), end)
                : action.substring(LEGACY_PREFIX.length());
        if (uri.isEmpty()) return null;
        try {
            return Intent.parseUri(uri, Intent.URI_INTENT_SCHEME | Intent.URI_ALLOW_UNSAFE);
        } catch (Exception e) {
            return null;
        }
    }

    /** Saves a shortcut picked by the user: legacy:&lt;intent uri&gt;\n&lt;label&gt;. */
    static String shortcutAction(Intent target, String label) {
        if (target == null) return null;
        String name = label != null && !label.isEmpty() ? label : "Shortcut";
        return LEGACY_PREFIX + target.toUri(Intent.URI_INTENT_SCHEME) + "\n" + name;
    }

    /**
     * Activities that can create launcher shortcuts, i.e. everything handling
     * android.intent.action.CREATE_SHORTCUT. Ordered A-Z by label; this is the
     * same list Nova Launcher shows under its gesture Shortcuts tab.
     */
    static List<ResolveInfo> shortcutProviders(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent query = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        List<ResolveInfo> found;
        try {
            found = pm.queryIntentActivities(query, 0);
        } catch (Exception e) {
            found = Collections.emptyList();
        }
        List<ResolveInfo> result = new ArrayList<>();
        if (found != null) {
            for (ResolveInfo info : found) {
                if (info.activityInfo != null) {
                    result.add(info);
                }
            }
        }
        Collections.sort(result, (a, b) ->
                providerLabel(context, a).compareToIgnoreCase(providerLabel(context, b)));
        return result;
    }

    /** Display name of one shortcut activity, falling back to its app's name. */
    static String providerLabel(Context context, ResolveInfo info) {
        try {
            CharSequence label = info.loadLabel(context.getPackageManager());
            if (label != null && label.length() > 0) return label.toString();
        } catch (Exception ignored) {
        }
        if (info.activityInfo != null) {
            return packageLabel(context, info.activityInfo.packageName);
        }
        return "Shortcut";
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
