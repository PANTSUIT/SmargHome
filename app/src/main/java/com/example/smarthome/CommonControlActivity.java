package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonToken;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class CommonControlActivity extends AppCompatActivity {

    private static final String TAG = "CommonControlActivity";
    XMqttService mqttService;
    ServiceConnection serviceConnection;

    Switch  switch_bedroom_lamp, switch_bedroom_fan, switch_bedroom_curtain;
    TextView textView_bedroom_humitity, textView_bedroom_temperature;

    Switch switch_livingroom_lamp, switch_livingroom_fan, switch_livingroom_curtain;
    TextView textView_livingroom_humitity, textView_livingroom_temperature;

    Switch switch_kitchen_lamp;
    TextView textView_kitchen_humitidy, textView_kitchen_temperature;

    Switch switch_toilet_lamp;

    Timer timer = new Timer();

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

        // bedroom fan
        switch_bedroom_fan = findViewById(R.id.switch_bedroom_fan);
        switch_bedroom_fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("fan", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_bedroom_fan), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // bedroom curtain
        switch_bedroom_curtain = findViewById(R.id.switch_bedroom_curtain);
        switch_bedroom_curtain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("curtain", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_bedroom_curtain), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // bedroom humitity
        textView_bedroom_humitity = findViewById(R.id.text_bedroom_humidity);

        // bedroom humitity
        textView_bedroom_temperature = findViewById(R.id.text_bedroom_temperature);

        // livingroom lamp
        switch_livingroom_lamp = findViewById(R.id.switch_livingroom_lamp);
        switch_livingroom_lamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lamp", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_livingroom_lamp), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // livingroom fan
        switch_livingroom_fan = findViewById(R.id.switch_livingroom_fan);
        switch_livingroom_fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("fan", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_livingroom_fan), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // livingroom curtain
        switch_livingroom_curtain = findViewById(R.id.switch_livingroom_curtain);
        switch_livingroom_curtain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("curtain", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_livingroom_curtain), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // livingroom humitdity
        textView_livingroom_humitity = findViewById(R.id.text_livingroom_humidiry);

        // livingroom temperature
        textView_livingroom_temperature = findViewById(R.id.text_livingroom_temperature);

        // kitchen lamp
        switch_kitchen_lamp = findViewById(R.id.switch_kitchen_lamp);
        switch_kitchen_lamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lamp", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_kitchen_lamp), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // kitchen humidity
        textView_kitchen_humitidy = findViewById(R.id.text_kitchen_humidiry);

        // kitchen temperature
        textView_kitchen_temperature = findViewById(R.id.text_kitchen_temperature);

        // toilet lamp
        switch_toilet_lamp = findViewById(R.id.switch_toilet_lamp);
        switch_toilet_lamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("lamp", isChecked ? 1 : 0);
                    mqttService.publishMessage(getString(R.string.topic_toilet_lamp), jsonObject.toString());
                } catch ( JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),String.format("humidity" + mqttService.getBedroom_humidity()), Toast.LENGTH_SHORT).show();

                /*
                textView_bedroom_humitity.setText(String.format("humidity" + mqttService.getBedroom_humidity()));
                textView_bedroom_temperature.setText(String.format("humidity" + mqttService.getBedroom_temperature()));
                textView_livingroom_humitity.setText(String.format("humidity" + mqttService.getLivingroom_humidity()));
                textView_livingroom_temperature.setText(String.format("humidity" + mqttService.getLivingroom_temperature()));
                textView_kitchen_humitidy.setText(String.format("humidity" + mqttService.getKitchen_humidity()));
                textView_kitchen_temperature.setText(String.format("humidity" + mqttService.getKitchen_temperature()));
                 */
            }
        }, 0, 1000);

    }
}