package com.autoreconnect;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etPingInterval, etAirplaneOn, etReconnectWait;
    private Button btnStart, btnStop;
    private TextView tvStatus;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        etPingInterval = findViewById(R.id.etPingInterval);
        etAirplaneOn = findViewById(R.id.etAirplaneOn);
        etReconnectWait = findViewById(R.id.etReconnectWait);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);

        // Load saved settings
        etPingInterval.setText(String.valueOf(prefs.getInt("ping_interval", 5)));
        etAirplaneOn.setText(String.valueOf(prefs.getInt("airplane_on", 5)));
        etReconnectWait.setText(String.valueOf(prefs.getInt("reconnect_wait", 15)));

        btnStart.setOnClickListener(v -> startService());
        btnStop.setOnClickListener(v -> stopService());

        updateStatus();
    }

    private void startService() {
        // Save settings
        int pingInterval = Integer.parseInt(etPingInterval.getText().toString());
        int airplaneOn = Integer.parseInt(etAirplaneOn.getText().toString());
        int reconnectWait = Integer.parseInt(etReconnectWait.getText().toString());

        prefs.edit()
            .putInt("ping_interval", pingInterval)
            .putInt("airplane_on", airplaneOn)
            .putInt("reconnect_wait", reconnectWait)
            .apply();

        Intent intent = new Intent(this, ReconnectService.class);
        intent.putExtra("ping_interval", pingInterval);
        intent.putExtra("airplane_on", airplaneOn);
        intent.putExtra("reconnect_wait", reconnectWait);
        startForegroundService(intent);

        tvStatus.setText("Status: AKTIF");
        Toast.makeText(this, "Auto Reconnect aktif!", Toast.LENGTH_SHORT).show();
    }

    private void stopService() {
        Intent intent = new Intent(this, ReconnectService.class);
        stopService(intent);
        tvStatus.setText("Status: MATI");
        Toast.makeText(this, "Auto Reconnect dihentikan.", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus() {
        boolean running = prefs.getBoolean("running", false);
        tvStatus.setText("Status: " + (running ? "AKTIF" : "MATI"));
    }
}
