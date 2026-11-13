package com.vanta.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                             WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#05070B"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 80, 40, 40);

        TextView brand = new TextView(this);
        brand.setText("VANTA");
        brand.setTextColor(Color.parseColor("#F4F7FC"));
        brand.setTextSize(26);
        brand.setLetterSpacing(0.25f);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("CYBER DEFENSE");
        sub.setTextColor(Color.parseColor("#00C8FF"));
        sub.setTextSize(11);
        sub.setLetterSpacing(0.4f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 4, 0, 30);
        root.addView(sub);

        ShieldView shield = new ShieldView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(600, 600);
        shield.setLayoutParams(lp);
        root.addView(shield);

        TextView score = new TextView(this);
        score.setText("98");
        score.setTextColor(Color.parseColor("#00F0A0"));
        score.setTextSize(48);
        score.setGravity(Gravity.CENTER);
        root.addView(score);

        TextView status = new TextView(this);
        status.setText("PROTECTION ACTIVE");
        status.setTextColor(Color.parseColor("#8996AA"));
        status.setTextSize(12);
        status.setLetterSpacing(0.3f);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, 24);
        root.addView(status);

        root.addView(row("Malware Protection", "ACTIVE", "#00F0A0"));
        root.addView(row("Application Security", "ACTIVE", "#00F0A0"));
        root.addView(row("Privacy Protection", "ACTIVE", "#00F0A0"));
        root.addView(row("Network Security", "MONITORING", "#FFB547"));

        setContentView(root);
    }

    private LinearLayout row(String title, String state, String color) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setBackgroundColor(Color.parseColor("#101827"));
        l.setPadding(32, 28, 32, 28);
        l.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 8, 0, 8);
        l.setLayoutParams(p);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor("#F4F7FC"));
        t.setTextSize(14);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        l.addView(t);

        TextView s = new TextView(this);
        s.setText(state);
        s.setTextColor(Color.parseColor(color));
        s.setTextSize(10);
        s.setLetterSpacing(0.2f);
        l.addView(s);

        return l;
    }

    // ===== الدرع المتحرك =====
    class ShieldView extends View {
        private Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint core = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint scan = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float angle = 0f;
        private float pulse = 0f;
        private Handler h = new Handler();

        ShieldView(Context c) {
            super(c);
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(3f);
            core.setStyle(Paint.Style.FILL);
            scan.setStyle(Paint.Style.STROKE);
            scan.setStrokeWidth(6f);
            scan.setColor(Color.parseColor("#00F0A0"));

            h.postDelayed(new Runnable() {
                public void run() {
                    angle = (angle + 2f) % 360f;
                    pulse = (float) (Math.sin(System.currentTimeMillis() / 600.0) * 0.5 + 0.5);
                    invalidate();
                    h.postDelayed(this, 33);
                }
            }, 33);
        }

        @Override
        protected void onDraw(Canvas cv) {
            int w = getWidth(), hh = getHeight();
            float cx = w / 2f, cy = hh / 2f;
            float r = Math.min(w, hh) * 0.36f;

            // هالات خارجية
            for (int i = 3; i >= 1; i--) {
                ring.setColor(Color.argb(30 + i * 10, 0, 200, 255));
                ring.setStrokeWidth(1.5f);
                cv.drawCircle(cx, cy, r + i * 22, ring);
            }

            // الحلقة الأساسية
            ring.setColor(Color.parseColor("#172235"));
            ring.setStrokeWidth(10f);
            cv.drawCircle(cx, cy, r, ring);

            // قوس تقدم نيون
            RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
            ring.setColor(Color.parseColor("#00F0A0"));
            ring.setStrokeWidth(10f);
            ring.setStrokeCap(Paint.Cap.ROUND);
            cv.drawArc(oval, -90, 352, false, ring);

            // نبضة داخلية
            core.setColor(Color.argb((int)(40 + pulse * 60), 0, 240, 160));
            cv.drawCircle(cx, cy, r * 0.55f, core);

            // خط المسح الدوّار
            cv.save();
            cv.rotate(angle, cx, cy);
            cv.drawLine(cx, cy - r * 0.6f, cx, cy + r * 0.6f, scan);
            cv.restore();

            // نقطة مضيئة على القوس
            double rad = Math.toRadians(-90 + 352 * 0.98);
            float px = (float) (cx + Math.cos(rad) * r);
            float py = (float) (cy + Math.sin(rad) * r);
            core.setColor(Color.parseColor("#F4F7FC"));
            cv.drawCircle(px, py, 7f, core);
        }
    }
                }
