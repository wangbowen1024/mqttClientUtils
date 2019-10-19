package com.aliyun.iot.demo;

import com.aliyun.openservices.iot.api.Profile;
import com.aliyun.openservices.iot.api.message.MessageClientFactory;
import com.aliyun.openservices.iot.api.message.api.MessageClient;
import com.aliyun.openservices.iot.api.message.callback.MessageCallback;
import com.aliyun.openservices.iot.api.message.entity.Message;
import com.aliyun.openservices.iot.api.message.entity.MessageToken;

/**
 * 设备数据上发--服务端
 */
public class H2SubServer {

    public static void main(String[] args) {
        // 身份
        String accessKey = "LTAI4Fe8hRhqfaexzT1rSKKR";
        String accessSecret = "JaYTp10VuRC7k8BacfFW4ZEnAuhspZ";
        String uid = "1295935431193801";
        String regionId = "cn-shanghai";
        String productKey = "a1i6fHay1uY";
        String deviceName = "BC28TEST";
        String endPoint = "https://" + uid + ".iot-as-http2." + regionId + ".aliyuncs.com";
        // 连接配置
        Profile profile = Profile.getAccessKeyProfile(endPoint, regionId, accessKey, accessSecret);

        // 构造客户端
        MessageClient client = MessageClientFactory.messageClient(profile);

        MessageCallback messageCallback = new MessageCallback() {
            @Override
            public Action consume(MessageToken messageToken) {
                Message m = messageToken.getMessage();
                System.out.println(messageToken.getMessage());
                return MessageCallback.Action.CommitSuccess;
            }
        };
        // 注册订阅监听器
        client.setMessageListener("/sys/" + productKey + "/" + deviceName + "/thing/event/user/property/post", messageCallback);

        // 数据接收
        client.connect(new MessageCallback() {
            @Override
            public Action consume(MessageToken messageToken) {
                Message m = messageToken.getMessage();

                // topic
                System.out.println("\ntopic=" + m.getTopic());
                // 解析JSON约定命名。这里添加到数据库
                System.out.println("payload=" + new String(m.getPayload()));
                // 时间
                System.out.println("generateTime=" + m.getGenerateTime());

                // 此处标记CommitSuccess已消费，IoT平台会删除当前Message，否则会保留到过期时间
                return MessageCallback.Action.CommitSuccess;
            }
        });
    }
}