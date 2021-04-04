package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonToken;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonControlActivity extends AppCompatActivity {

    private static final String TAG = "CommonControlActivity";
    XMqttService mqttService;
    ServiceConnection serviceConnection;

    Switch  switch_bedroom_lamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_control);

        // XMqttService Connect
        Intent serviceIntent = new Intent(this, XMqttService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                XMqttService.XBinder binder = (XMqttService.XBinder) service;
                mqttService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mqttService = null;
            }
        };
        this.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);


        switch_bedroom_lamp = findViewById(R.id.switch_bedroom_lamp);
        switch_bedroom_lamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lamp", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_bedroom_lamp), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}