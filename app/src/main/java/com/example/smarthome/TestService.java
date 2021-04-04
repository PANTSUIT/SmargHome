package com.example.smarthome;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.net.ConnectException;

public class TestService extends Service {
    private static final String TAG = "TestService";

    public static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    public String HOST = "tcp://111.230.206.15:1883";
    public String USERNAME = "panda";
    public String Passwd  = "panda";
    public char PASSWORD[] = {'p','a','n','d','a'};
    public static String PUBLIC_TOPIC = "tourist_enter";
    public static String RESPONSE_TOPIC = "message_arrived";
    public String CLIENTID = "APP";

    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "连接断开");
            Toast.makeText(getApplicationContext(), "连接断开", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "收到消息:" + new String(message.getPayload()));
            // 收到消息， 这里弹出Toast表示，如果需要更新UI，可以使用关白或则 EventBus进行发送
            Toast.makeText(getApplicationContext(), "messageArrived:" + new String(message.getPayload()), Toast.LENGTH_SHORT).show();

            // 收到其他客户段的消息后， 响应给对方告知消息已到达，或者有问题等
            // response("messge arrived");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return null;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");

        try{
            mqttAndroidClient.disconnect();
        }catch (MqttException ex){
            ex.printStackTrace();
        }
        super.onDestroy();
    }

    // MQTT 是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Toast.makeText(getApplicationContext(), "Connect Success", Toast.LENGTH_SHORT).show();
            try{
                 mqttAndroidClient.subscribe("/test/test", 0);
            }
            catch (MqttException ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Toast.makeText(getApplicationContext(), "Connect Failed", Toast.LENGTH_SHORT).show();
            connectToServer(); // 连接失败，重连(可关闭服务器进行模拟)
        }
    };

    /**
     * 判断网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
        if(manager == null){
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    /**
     * 连接MQTT服务器
     */
    public void connectToServer() {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, iMqttActionListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        Toast.makeText(this, "Mqtt Init", Toast.LENGTH_SHORT).show();

        String serverURI = HOST; // 服务器地址(协议 + 地址 + 端口号)
        mqttAndroidClient = new MqttAndroidClient(this, serverURI, CLIENTID);
        mqttAndroidClient.setCallback(mqttCallback);

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true); // 是否清除缓存
        mqttConnectOptions.setConnectionTimeout(20);  // 设置超时时间，单位：秒
        mqttConnectOptions.setKeepAliveInterval(10); // 设置心跳包发送间隔，单位：秒
        mqttConnectOptions.setUserName(USERNAME); // 设置用户名
        mqttConnectOptions.setPassword(PASSWORD); // 设置密码


        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" +CLIENTID+ "\"}";
        String topic = PUBLIC_TOPIC;
        Integer qos = 2;
        Boolean retained = false;

        mqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        /*
        if((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            try{
                mqttConnectOptions.setWill(topic, message.getBytes(),qos.intValue(),retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        if(doConnect) {
            doClientConnection();
        }
        */
        if(isNetworkAvailable(this))
        {
            Toast.makeText(getApplicationContext(), "Network is available", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Network is not available", Toast.LENGTH_SHORT).show();
        }
        connectToServer();
    }


    public void publish(String payload, int qos) {
        try {
            if(mqttAndroidClient.isConnected() == false)
            {
                connectToServer();
            }
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(qos);
            mqttAndroidClient.publish(PUBLIC_TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException ex){
            Log.e(TAG, ex.toString());
            ex.printStackTrace();
        }
    }

    public class MyBinder extends Binder {
        public TestService getService()
        {
            return TestService.this;
        }
    }

}