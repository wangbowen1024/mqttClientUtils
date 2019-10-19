package com.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.HashMap;
import java.util.Map;

/**
 * MQTT客户端抽象类
 *
 * @author BoWenWang
 */
public abstract class AbstractMqttClient {
    /**
     * MQTT服务器地址
     */
    private String serviceUri;
    /**
     * MQTT客户端ID
     */
    private String clientId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 是否清除会话(默认清楚)
     */
    private boolean isCleanSession;
    /**
     * 超时时间
     */
    private int timeout;
    /**
     * 心跳检测时间
     */
    private int aliveInterval;
    /**
     * 客户端实例
     */
    private MqttClient mqttClient;
    /**
     * 客户端连接参数对象
     */
    private MqttConnectOptions mqttConnectOptions;
    /**
     * 订阅主题列表（重连后会断开所有订阅）
     */
    private Map<String, Integer> topicMap;

    public AbstractMqttClient(String serviceUri, String clientId, String userName, String password) {
        this(serviceUri, clientId, userName, password, true);
    }

    public AbstractMqttClient(String serviceUri, String clientId, String userName, String password, boolean isCleanSession) {
        this(serviceUri, clientId, userName, password, isCleanSession, 10, 20);
    }

    public AbstractMqttClient(String serviceUri, String clientId, String userName, String password, boolean isCleanSession, int timeout, int aliveInterval) {
        this.serviceUri = serviceUri;
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
        this.isCleanSession = isCleanSession;
        this.timeout = timeout;
        this.aliveInterval = aliveInterval;
        topicMap = new HashMap<String, Integer>(16);
        init();
    }

    /**
     * 初始化客户端
     */
    public void init() {
        try {
            // 客户端连接参数对象
            mqttConnectOptions = new MqttConnectOptions();
            // 内存存储
            MqttClientPersistence persistence = new MemoryPersistence();
            // 创建客户端
            mqttClient = new MqttClient(serviceUri, clientId, persistence);
            // 在重新启动和重新连接时记住状态
            mqttConnectOptions.setCleanSession(isCleanSession);
            // 设置连接的用户名、密码
            mqttConnectOptions.setUserName(userName);
            mqttConnectOptions.setPassword(password.toCharArray());
            // 设置超时时间 单位为秒
            mqttConnectOptions.setConnectionTimeout(timeout);
            // 设置会话心跳时间 单位为秒 服务器会每隔一定时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            mqttConnectOptions.setKeepAliveInterval(aliveInterval);
            // 设置回调函数
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("MQTT客户端(" + clientId + ")连接丢失！");
                    reConnect(5);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    try {
                        processMessage(topic, message);
                    } catch (Exception e) {
                        System.out.println("处理主题\"" + topic + "\"的消息\"" + message + "\"失败");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            // 建立连接
            mqttClient.connect(mqttConnectOptions);
        } catch (MqttException e) {
            mqttClient = null;
        }
        if (null != mqttClient && mqttClient.isConnected()) {
            System.out.println("MQTT客户端(" + clientId + ")初始化成功！");
        } else {
            System.out.println("MQTT客户端(" + clientId + ")初始化失败！");
        }
    }

    /**
     * 连接断开重连（默认5次）
     * @param time  剩余重连次数
     */
    private void reConnect(int time) {
        if (time == 0) {
            System.out.println("MQTT客户端(" + clientId + ")重连失败！");
            mqttClient = null;
            return;
        }
        System.out.println("MQTT客户端(" + clientId + ")正在尝试重连...(剩余" + time + "次)");
        try {
            if(null != mqttClient) {
                if(!mqttClient.isConnected()) {
                    if(null != mqttConnectOptions) {
                        mqttClient.connect(mqttConnectOptions);
                        if (mqttClient.isConnected()) {
                            System.out.println("MQTT客户端(" + clientId + ")重连成功！");
                            for (Map.Entry<String, Integer> entry : topicMap.entrySet()) {
                                subscribe(entry.getKey(), entry.getValue());
                            }
                            return;
                        }
                    }else {
                        System.out.println("mqttConnectOptions is null");
                    }
                }else {
                    System.out.println("mqttClient is null or connect");
                }
            }else {
                init();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        reConnect(time - 1);
    }

    /**
     * MQTT消息处理函数
     * @param topic     主题
     * @param message   MQTT消息
     */
    abstract void processMessage(String topic, MqttMessage message);

    /**
     * 发布消息
     * @param topic     主题
     * @param content   内容
     * @param qos       消息服务质量
     */
    public void publish(String topic, String content, int qos) {
        if (null != mqttClient && mqttClient.isConnected()) {
            try {
                // 创建消息
                MqttMessage mqttMessage = new MqttMessage(content.getBytes());
                // 设置消息的服务质量
                mqttMessage.setQos(qos);
                // 发布信息
                MqttTopic mqttTopic = mqttClient.getTopic(topic);
                MqttDeliveryToken deliveryToken = mqttTopic.publish(mqttMessage);
                if (!deliveryToken.isComplete()) {
                    System.out.println("【成功】MQTT客户端(" + clientId + ")向\"" + topic + "\"主题发送：" + content);
                    deliveryToken.waitForCompletion();
                } else {
                    System.out.println("【失败】MQTT客户端(" + clientId + ")向\"" + topic + "\"主题发送：" + content);

                }
            } catch (MqttException me) {
                System.out.println("reason:" + me.getReasonCode());
                System.out.println("msg:" + me.getMessage());
                System.out.println("loc:" + me.getLocalizedMessage());
                System.out.println("cause:" + me.getCause());
                System.out.println("exception:" + me);
                me.printStackTrace();
            }
        } else {
            init();
        }
    }

    public void publish(String topic, String content) {
        publish(topic, content, 1);
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param qos   消息服务质量
     */
    public void subscribe(String topic, int qos) {
        if (null != mqttClient && mqttClient.isConnected()) {
            try {
                //订阅者连接订阅主题
                mqttClient.subscribe(topic, qos);
                topicMap.put(topic, qos);
                System.out.println("MQTT客户端(" + clientId + ")订阅\"" + topic + "\"主题成功");
            } catch (MqttException e) {
                System.out.println("MQTT客户端(" + clientId + ")订阅\"" + topic + "\"主题失败");
            }
        } else {
            init();
        }
    }

    public void subscribe(String topic) {
        subscribe(topic, 1);
    }

    /**
     * 取消订阅
     * @param topic 主题
     */
    public void unsubscribe(String topic) {
        if (null != mqttClient && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
                System.out.println("MQTT客户端(" + clientId + ")取消订阅\"" + topic + "\"主题成功");
            } catch (MqttException e) {
                System.out.println("MQTT客户端(" + clientId + ")取消订阅\"" + topic + "\"主题失败");
            }
        } else {
            init();
        }
    }

    /**
     * 取消所有订阅
     */
    public void unsubscribeAll() {
        for (String topic : topicMap.keySet()) {
            unsubscribe(topic);
        }
    }

    /**
     * 断开MQTT客户端连接
     */
    public void disconnect() {
        try {
            // 断开连接
            if (mqttClient != null) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            System.out.println("MQTT客户端(" + clientId + ")断开连接失败！");
        }
    }

    /**
     * 关闭MQTT客户端
     */
    public void close() {
        try {
            // 关闭客户端
            if (mqttClient != null) {
                mqttClient.close();
            }
        } catch (MqttException e) {
            System.out.println("MQTT客户端(" + clientId + ")关闭失败！");
        }
    }


    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCleanSession() {
        return isCleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        isCleanSession = cleanSession;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getAliveInterval() {
        return aliveInterval;
    }

    public void setAliveInterval(int aliveInterval) {
        this.aliveInterval = aliveInterval;
    }

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }

    public Map<String, Integer> getTopicMap() {
        return topicMap;
    }

    public void setTopicMap(Map<String, Integer> topicMap) {
        this.topicMap = topicMap;
    }
}
