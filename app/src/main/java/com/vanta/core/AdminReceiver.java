package com.vanta.core;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public CharSequence onDisableRequested(Context c, Intent i) {
        return "لا يمكن إزالة التطبيق — تواصل مع الدعم.";
    }
}
