package com.example.smarthome;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
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
import org.json.JSONObject;

public class XMqttService extends Service {

    private static final String TAG = "XMqttService";

    public static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    public String host = "tcp://111.230.206.15:1883";
    public String userName = "panda";
    public String passWord  = "panda";
    public static String PUBLIC_TOPIC = "tourist_enter";
    public static String RESPONSE_TOPIC = "message_arrived";
    public String clientId = "APP";

    private double bedroom_humidity = 0, bedroom_temperature = 0;
    private double livingroom_humidity = 0, livingroom_temperature = 0;
    private double kitchen_humidity = 0, kitchen_temperature = 0;


    private MqttCallback mqttCallback = new MqttCallback() {
        //连接异常断开后，调用
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG, "connection lost");
        }
        //消息到达后，调用 接收
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            Toast.makeText(getApplicationContext(), "messageArrived:" + topic+ new String(message.getPayload()), Toast.LENGTH_SHORT).show();

            JSONObject jsonObject = new JSONObject(message.toString());
            if(topic.equals(getString(R.string.topic_bedroom_humidity)))
            {
                bedroom_humidity = jsonObject.getDouble("humidity");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + bedroom_humidity), Toast.LENGTH_SHORT).show();
            }
            else if(topic.equals(getString(R.string.topic_bedroom_temperature)))
            {
                bedroom_temperature = jsonObject.getDouble("temperature");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + bedroom_temperature), Toast.LENGTH_SHORT).show();
            }
            else if(topic.equals(getString(R.string.topic_livingroom_humidity)))
            {
                livingroom_humidity = jsonObject.getDouble("temperature");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + livingroom_humidity), Toast.LENGTH_SHORT).show();
            }
            else if(topic.equals(getString(R.string.topic_livingroom_temperature)))
            {
                livingroom_temperature = jsonObject.getDouble("temperature");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + livingroom_temperature), Toast.LENGTH_SHORT).show();
            }
            else if(topic.equals(getString(R.string.topic_kitchen_humidity)))
            {
                kitchen_humidity = jsonObject.getDouble("temperature");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + kitchen_humidity), Toast.LENGTH_SHORT).show();
            }
            else if(topic.equals(getString(R.string.topic_kitchen_temperature)))
            {
                kitchen_temperature = jsonObject.getDouble("temperature");
                Toast.makeText(getApplicationContext(), String.format("messageArrived: " + kitchen_temperature), Toast.LENGTH_SHORT).show();
            }
        }
        //消息发送成功后，调用
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "msg delivered");
        }
    };

    private XBinder binder = new XBinder();


    public XMqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return this.binder;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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

    public class XBinder extends Binder {
        public XMqttService getService()
        {
            return XMqttService.this;
        }
    }

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
            if(!isNetworkAvailable(this))
            {
                Toast.makeText(getApplicationContext(), "Network is not available!!!", Toast.LENGTH_SHORT).show();
            }

            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Connect succeed!", Toast.LENGTH_SHORT).show();

                    subscribeTopic(getString(R.string.topic_bedroom_fan), 0);
                    subscribeTopic(getString(R.string.topic_bedroom_lamp), 0);
                    subscribeTopic(getString(R.string.topic_bedroom_curtain), 0);
                    subscribeTopic(getString(R.string.topic_bedroom_humidity), 0);
                    subscribeTopic(getString(R.string.topic_bedroom_temperature), 0);

                    subscribeTopic(getString(R.string.topic_livingroom_fan), 0);
                    subscribeTopic(getString(R.string.topic_livingroom_lamp), 0);
                    subscribeTopic(getString(R.string.topic_livingroom_curtain), 0);
                    subscribeTopic(getString(R.string.topic_livingroom_humidity), 0);
                    subscribeTopic(getString(R.string.topic_livingroom_temperature), 0);

                    subscribeTopic(getString(R.string.topic_kitchen_lamp), 0);
                    subscribeTopic(getString(R.string.topic_kitchen_humidity), 0);
                    subscribeTopic(getString(R.string.topic_kitchen_temperature), 0);

                    subscribeTopic(getString(R.string.topic_toilet_lamp), 0);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Connect failed!", Toast.LENGTH_SHORT).show();
                    //connectToServer();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void init() {

        /* 创建MqttConnectOptions对象并配置username和password */
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());

        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(mqttCallback);

        /* Mqtt建连 */
        connectToServer();
    }

    public void setHost(String ip)
    {
        host = "tcp://" + ip + ":1883";
        Toast.makeText(getApplicationContext(), host, Toast.LENGTH_SHORT).show();
    }

    public Boolean isConnected()
    {
        return mqttAndroidClient.isConnected();
    }

    /**
     * 订阅特定的主题
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic, int qos) {
        try {
            mqttAndroidClient.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向默认的主题/user/update发布消息
     * @param payload 消息载荷
     */
    public void publishMessage(String topic, String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                // mqttAndroidClient.connect();
                connectToServer();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public static MqttAndroidClient getMqttAndroidClient() {
        return mqttAndroidClient;
    }

    public double getBedroom_humidity() {
        return bedroom_humidity;
    }

    public double getBedroom_temperature() {
        return bedroom_temperature;
    }

    public double getKitchen_humidity() {
        return kitchen_humidity;
    }

    public double getKitchen_temperature() {
        return kitchen_temperature;
    }

    public double getLivingroom_humidity() {
        return livingroom_humidity;
    }

    public double getLivingroom_temperature() {
        return livingroom_temperature;
    }
}