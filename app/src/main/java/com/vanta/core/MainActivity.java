package com.vanta.core;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView tv = new TextView(this);
        tv.setText("VANTA\nIt Works!");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(40);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setBackgroundColor(Color.BLACK);
        setContentView(tv);
    }
}
