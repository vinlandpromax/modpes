package com.autoreconnect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

public class ReconnectService extends Service {

    private static final String TAG = "AutoReconnect";
    private static final String CHANNEL_ID = "auto_reconnect";
    private boolean running = false;
    private Thread monitorThread;

    private int pingInterval = 5;
    private int airplaneOnDuration = 5;
    private int reconnectWait = 15;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            pingInterval = intent.getIntExtra("ping_interval", 5);
            airplaneOnDuration = intent.getIntExtra("airplane_on", 5);
            reconnectWait = intent.getIntExtra("reconnect_wait", 15);
        }

        createNotificationChannel();
        startForeground(1, buildNotification("Monitoring koneksi..."));

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean("running", true).apply();

        running = true;
        monitorThread = new Thread(this::monitor);
        monitorThread.start();

        return START_STICKY;
    }

    private void monitor() {
        while (running) {
            try {
                if (isConnected()) {
                    updateNotification("✓ Connected");
                    Log.d(TAG, "Connected");
                } else {
                    Log.d(TAG, "Disconnected! Toggling airplane mode...");
                    updateNotification("✗ Disconnect - Toggle airplane mode...");
                    toggleAirplane(true);
                    Thread.sleep(airplaneOnDuration * 1000L);
                    toggleAirplane(false);
                    updateNotification("Menunggu reconnect...");
                    Thread.sleep(reconnectWait * 1000L);

                    if (isConnected()) {
                        updateNotification("✓ Reconnect berhasil!");
                        Log.d(TAG, "Reconnect success!");
                    } else {
                        updateNotification("✗ Masih gagal, coba lagi...");
                        Log.d(TAG, "Still disconnected, retrying...");
                    }
                }
                Thread.sleep(pingInterval * 1000L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private boolean isConnected() {
        try {
            InetAddress addr = InetAddress.getByName("1.1.1.1");
            return addr.isReachable(3000);
        } catch (IOException e) {
            return false;
        }
    }

    private void toggleAirplane(boolean enable) {
        try {
            Settings.Global.putInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, enable ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", enable);
            sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Gagal toggle airplane: " + e.getMessage());
        }
    }

    private void updateNotification(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, buildNotification(text));
    }

    private Notification buildNotification(String text) {
        return new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto Reconnect")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "Auto Reconnect", NotificationManager.IMPORTANCE_LOW);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        running = false;
        if (monitorThread != null) monitorThread.interrupt();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean("running", false).apply();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
