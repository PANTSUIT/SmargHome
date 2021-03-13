package com.example.smarthome;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;

public class MyMqttService extends Service {

    public final  String TAG = MyMqttService.class.getSimpleName();

    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    public String HOST = "Tcp://111.230.206.15:8083";
    public String USERNAME = "panda";
    public char PASSWORD[] = {'p','a','n','d','a'};
    public static String PUBLIC_TOPIC = "tourist_enter";
    public static String RESPONSE_TOPIC = "message_arrived";

    @RequiresApi(api = 26)
    public String CLIENTID = "APP";

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        init();
        return super.onStartCommand(intent, flags, startID);
    }

    public MyMqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * 开启服务
     */
    public static void startService(Context mContext) {
        mContext.startService(new Intent(mContext, MyMqttService.class));
    }

    /**
     * 发布 (模拟其他客户端发布消息)
     * @param message 消息
     */
    public static void publish(String message) {
        String topic = PUBLIC_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        try{
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 响应 (收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等)
     *
     * @param message 消息
     */
    public void response(String message) {
        String topic = RESPONSE_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        try{
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init() {
        String serverURI = HOST; // 服务器地址(协议 + 地址 + 端口号)
        mqttAndroidClient = new MqttAndroidClient(this, serverURI, CLIENTID);
        mqttAndroidClient.setCallback(mqttCallback);

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true); // 是否清除缓存
        mqttConnectOptions.setConnectionTimeout(10);  // 设置超时时间，单位：秒
        mqttConnectOptions.setKeepAliveInterval(20); // 设置心跳包发送间隔，单位：秒
        mqttConnectOptions.setUserName(USERNAME); // 设置用户名
        mqttConnectOptions.setPassword(PASSWORD); // 设置密码

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" +CLIENTID+ "\"}";
        String topic = PUBLIC_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
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
    }

    /**
     * 连接MQTT服务器
     */
    private  void doClientConnection() {
        if(!mqttAndroidClient.isConnected() && isConnectIsNormal()) {
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "当前网络名称:" + name);
            return true;
        }
        else {
            Log.i(TAG,"没有可用网络");
            /* 没有可用网络的时候，延迟3秒再尝试重连 */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doClientConnection();
                }
            }, 3000);
            return false;
        }
    }

    // MQTT 是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG, "连接成功");
            try{
                mqttAndroidClient.subscribe(PUBLIC_TOPIC, 2); //订阅主题，参数：主题、服务质量
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            Log.i(TAG, "连接失败");
            doClientConnection(); // 连接失败，重连(可关闭服务器进行模拟)
        }
    };

    // 订阅主题的回调
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "连接断开");
            doClientConnection();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "收到消息:" + new String(message.getPayload()));
            // 收到消息， 这里弹出Toast表示，如果需要更新UI，可以使用关白或则 EventBus进行发送
            Toast.makeText(getApplicationContext(), "messageArrived:" + new String(message.getPayload()), Toast.LENGTH_SHORT).show();

            // 收到其他客户段的消息后， 响应给对方告知消息已到达，或者有问题等
            response("messge arrived");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };


    @Override
    public void onDestroy() {
        try{
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();;
        }
        super.onDestroy();
    }


}