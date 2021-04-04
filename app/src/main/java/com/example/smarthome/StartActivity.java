package com.example.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private Button button_voicectrl;
    private Button button_commonctrl;

    TestService mqttService;
    ServiceConnection serviceConnection;

    /* 自动Topic, 用于上报消息 */
    final private String PUB_TOPIC = "/user/update";
    /* 自动Topic, 用于接受消息 */
    final private String SUB_TOPIC = "/user/get";

    /* 阿里云Mqtt服务器域名 */
    final String host = "tcp://111.230.206.15:1883";
    private String clientId;
    private String userName;
    private String passWord;

    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.i(TAG, "onCreate: ");
        button_voicectrl = findViewById(R.id.button_reconized);
        button_commonctrl = findViewById(R.id.button_common);

        final Intent intent = new Intent(StartActivity.this , TestService.class);

        button_voicectrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button_voicdctrl clicked");
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        button_commonctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "button_commonctrl clicked");
                Intent intent = new Intent(StartActivity.this, CommonControlActivity.class);
                startActivity(intent);
            }
        });

        Intent serviceIntent = new Intent(this, MyMqttService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                TestService.MyBinder binder = (TestService.MyBinder) service;
                mqttService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mqttService = null;
            }
        };

        Intent intentOne = new Intent(this, TestService.class);
        startService(intentOne);
        // mqttinit();
    }


    @Override
    protected void onDestroy() {
        Intent intentOne = new Intent(this, TestService.class);
        stopService(intentOne);
        super.onDestroy();
    }

    public void mqttinit(){
        // MQTT
        /* 获取Mqtt建连信息clientId, username, password */
        IotMqttOption iotMqttOption = new IotMqttOption();
        if (iotMqttOption == null) {
            Log.e(TAG, "device info error");
        } else {
            clientId = iotMqttOption.getClientId();
            userName = iotMqttOption.getUsername();
            passWord = iotMqttOption.getPassword();
        }

        /* 创建MqttConnectOptions对象并配置username和password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());

        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
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
            }
            //消息发送成功后，调用
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });
        /* Mqtt建连 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");
                    Toast.makeText(getApplicationContext(), "Connect succeed!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                    Toast.makeText(getApplicationContext(), "Connect failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅特定的主题
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
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
                mqttAndroidClient.connect();
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

    /**
     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     */
    class IotMqttOption {
        private String username = "";
        private String password = "";
        private String clientId = "";

        public String getUsername() { return this.username;}
        public String getPassword() { return this.password;}
        public String getClientId() { return this.clientId;}

        /**
         * 获取Mqtt建连选项对象
         * @return AiotMqttOption对象或者NULL
         */
        public IotMqttOption()
        {
            try {
                String timestamp = Long.toString(System.currentTimeMillis());
                // clientId
                this.clientId = "APP";
                // userName
                this.username = "panda";
                // password
                this.password = "panda";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}