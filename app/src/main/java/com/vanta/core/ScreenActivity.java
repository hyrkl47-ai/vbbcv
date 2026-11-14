package com.vanta.core;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public class ScreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        MediaProjectionManager m = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(m.createScreenCaptureIntent(), 100);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        if (r == 100 && c == RESULT_OK) {
            Intent i = new Intent(this, ScreenService.class);
            i.putExtra("code", c);
            i.putExtra("data", d);
            startForegroundService(i);
        }
        finish();
    }
}
