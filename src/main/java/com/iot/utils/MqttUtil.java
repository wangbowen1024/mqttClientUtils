package com.iot.utils;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Map;
import java.util.UUID;

/**
 * MQTT订阅发布工具类
 *
 * @author BoWenWang
 */
public class MqttUtil {
    private static String serviceURI = "tcp://xxx.xxx.xxx.xxx:1883";
    private static String clientID = UUID.randomUUID().toString();
    private static MqttClientPersistence persistence = new MemoryPersistence();
    /**
     * 如果mqtt服务配置了匿名访问，则不需要使用用户名和密码就可以实现消息的订阅和发布
     */
    private static String username = "test";
    private static String password = "test";
    /**
     * 消息服务质量，一共有三个：
     *         0：尽力而为。消息可能会丢，但绝不会重复传输
     *         1：消息绝不会丢，但可能会重复传输
     *         2：恰好一次。每条消息肯定会被传输一次且仅传输一次
     */
    private static int qos = 1;

    private static MqttClient client;

    /**
     * 客户端初始化
     */
    public static void init() {
        try {
            client = new MqttClient(serviceURI, clientID, persistence);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息发布
     *
     * @author wzq
     **/
    public static void publish(String topic, String msg) {
        try {
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(username);
            connectOptions.setPassword(password.toCharArray());
            // 在重新启动和重新连接时记住状态
            connectOptions.setCleanSession(false);
            //发布者连接服务
            client.connect(connectOptions);
            System.out.println("发布者连接状态(" + clientID + ")： " + client.isConnected());
            MqttTopic mqttTopic = client.getTopic(topic);
            // 创建消息
            MqttMessage mqttMessage = new MqttMessage();
            // 设置消息的服务质量
            mqttMessage.setQos(qos);
            mqttMessage.setPayload(msg.getBytes());
            // 发布信息
            MqttDeliveryToken deliveryToken = mqttTopic.publish(mqttMessage);
            System.out.println("发布者(" + clientID + ")发布消息： " + msg);
            if (!deliveryToken.isComplete()) {
                System.out.println("【成功】发布者发布消息： " + msg);
                deliveryToken.waitForCompletion();
            } else {
                System.out.println("【失败】发布者发布消息： " + msg);
            }
            // 断开连接
            client.disconnect();
            // 关闭客户端
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息订阅
     *
     * @author wzq
     **/
    public static void subscribe(String topic) {
        try {
            MqttClient client = new MqttClient(serviceURI, clientID, persistence);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println(clientID + "：连接丢失...");
                    System.out.println(cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println(clientID + " 接收到消息： " + message.toString());
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(message.toString());
                        System.out.println("-------------begin-----------");
                        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                            System.out.println(entry.getKey() + " : " + entry.getValue());
                        }
                        System.out.println("--------------end------------");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(username);
            connectOptions.setPassword(password.toCharArray());
            connectOptions.setCleanSession(false);
            //订阅者连接订阅主题
            client.connect(connectOptions);
            client.subscribe(topic, qos);
            System.out.println(clientID + "订阅者连接状态： " + client.isConnected());
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
