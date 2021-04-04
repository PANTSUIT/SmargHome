package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private Button button_connect;
    private Button button_voicectrl;
    private Button button_commonctrl;
    private EditText editText_host;

    XMqttService mqttService;
    ServiceConnection serviceConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.i(TAG, "onCreate: ");

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

        // Start XMqttService
        startService(serviceIntent);

        // voice control
        button_voicectrl = findViewById(R.id.button_reconized);
        button_voicectrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button_voicdctrl clicked");
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // conmon control
        button_commonctrl = findViewById(R.id.button_common);
        button_commonctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button_commonctrl clicked");
                Intent intent = new Intent(StartActivity.this, CommonControlActivity.class);
                startActivity(intent);
            }
        });


        // Set host
        editText_host = findViewById(R.id.editText_host);

        // connnect
        button_connect = findViewById(R.id.button_connect);
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttService.setHost(editText_host.getText().toString());
                mqttService.init();
            }
        });


    }

    @Override
    protected void onDestroy() {
        Intent serviceIntent = new Intent(this, XMqttService.class);
        stopService(serviceIntent);
        super.onDestroy();
    }
}