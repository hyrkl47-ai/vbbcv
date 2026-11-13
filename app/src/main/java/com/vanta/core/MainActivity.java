package com.vanta.core;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        TextView tv = new TextView(this);
        tv.setText("VANTA\nالنظام يعمل بنجاح");
        tv.setTextColor(Color.GREEN);
        tv.setTextSize(30);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.BLACK);

        setContentView(tv);
    }
}
