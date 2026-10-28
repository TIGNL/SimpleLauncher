package com.simplelauncher;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class LauncherAccessibilityService extends AccessibilityService {

    private static LauncherAccessibilityService instance;

    public static LauncherAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /** Runs one of the accessibility actions chosen on the Gestures page. */
    public boolean perform(String action) {
        switch (action) {
            case "a11y:notifications":
                return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
            case "a11y:quick_settings":
                return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
            case "a11y:recents":
                return performGlobalAction(GLOBAL_ACTION_RECENTS);
            case "a11y:power_dialog":
                return performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
            case "a11y:lock_screen":
                return Build.VERSION.SDK_INT >= 28 && performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
            case "a11y:screenshot":
                return Build.VERSION.SDK_INT >= 28 && performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
            case "a11y:dismiss_shade":
                return Build.VERSION.SDK_INT >= 31 && performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
            case "a11y:accessibility_shortcut":
                return Build.VERSION.SDK_INT >= 31 && performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT);
            default:
                return false;
        }
    }
}
