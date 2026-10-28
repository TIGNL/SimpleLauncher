package com.simplelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;

/**
 * Central definitions for the home screen gestures feature: default action per
 * gesture, labels for the action list, and the intents behind shortcut actions.
 */
final class GestureActions {

    static final String NONE = "none";
    static final String CHOOSE_APP = "choose_app";

    private GestureActions() {}

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

            case "intent:airplane": return "Airplane mode settings";
            case "intent:alarm": return "Alarm";
            case "intent:assistant": return "Assistant";
            case "intent:bluetooth": return "Bluetooth settings";
            case "intent:camera": return "Camera";
            case "intent:datausage": return "Data usage settings";
            case "intent:datetime": return "Date & time settings";
            case "intent:display": return "Display settings";
            case "intent:sound": return "Sound settings";
            case "intent:websearch": return "Web search";
            case "intent:wifi": return "Wi-Fi settings";

            default:
                if (action.startsWith("app:")) {
                    return appLabel(context, action.substring(4));
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

    /** Shortcuts are plain Android intents, so no permission is needed beyond ours. */
    static Intent intentFor(String action) {
        if (action == null || !action.startsWith("intent:")) return null;
        switch (action.substring(7)) {
            case "wifi": return new Intent(Settings.ACTION_WIFI_SETTINGS);
            case "bluetooth": return new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            case "sound": return new Intent(Settings.ACTION_SOUND_SETTINGS);
            case "display": return new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            case "datetime": return new Intent(Settings.ACTION_DATE_SETTINGS);
            case "airplane": return new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            case "datausage": return new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
            case "alarm": return new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            case "camera": return new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            case "websearch": return new Intent(Intent.ACTION_WEB_SEARCH);
            case "assistant": return new Intent(Intent.ACTION_VOICE_COMMAND);
            default: return null;
        }
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

    /** The shortcuts this build offers, already ordered A-Z. */
    static String[][] shortcutActions() {
        return new String[][]{
                {"intent:airplane", "Airplane mode settings"},
                {"intent:alarm", "Alarm"},
                {"intent:assistant", "Assistant"},
                {"intent:bluetooth", "Bluetooth settings"},
                {"intent:camera", "Camera"},
                {"intent:datausage", "Data usage settings"},
                {"intent:datetime", "Date & time settings"},
                {"intent:display", "Display settings"},
                {"intent:sound", "Sound settings"},
                {"intent:websearch", "Web search"},
                {"intent:wifi", "Wi-Fi settings"},
        };
    }
}
