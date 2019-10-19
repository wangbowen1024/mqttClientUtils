package com.mqtt;

import com.alibaba.fastjson.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;

/**
 * MQTT客户端处理测试类
 * @author BoWenWang
 */
public class MyMqttClient extends AbstractMqttClient {

    public MyMqttClient(String serviceUri, String clientId, String userName, String password) {
        super(serviceUri, clientId, userName, password);
    }

    public MyMqttClient(String serviceUri, String clientId, String userName, String password, boolean isCleanSession) {
        super(serviceUri, clientId, userName, password, isCleanSession);
    }

    public MyMqttClient(String serviceUri, String clientId, String userName, String password, boolean isCleanSession, int timeout, int aliveInterval) {
        super(serviceUri, clientId, userName, password, isCleanSession, timeout, aliveInterval);
    }

    /**
     * 开发者自定义订阅消息回调处理函数
     * @param topic     主题
     * @param message   MQTT消息
     */
    @Override
    void processMessage(String topic, MqttMessage message) {
        System.out.println("-------------begin-----------");
        System.out.println("MQTT客户端(" + super.getClientId() + ")接收到\"" + topic + "\"主题的消息： " + message.toString());
        JSONObject jsonObject = JSONObject.parseObject(message.toString());
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("--------------end------------");
    }
}
