package com.example.smarthome;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyMqttService extends Service {

    public final  String TAG = MyMqttService.class.getSimpleName();

    private static MqttAndroidClient mqttAndroidClient;

    public MyMqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



}