package com.aliyun.iot.demo;

import com.aliyun.openservices.iot.api.Profile;
import com.aliyun.openservices.iot.api.message.MessageClientFactory;
import com.aliyun.openservices.iot.api.message.api.MessageClient;
import com.aliyun.openservices.iot.api.message.callback.MessageCallback;
import com.aliyun.openservices.iot.api.message.entity.Message;
import com.aliyun.openservices.iot.api.message.entity.MessageToken;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.iot.model.v20180120.PubRequest;
import com.aliyuncs.iot.model.v20180120.PubResponse;
import com.aliyuncs.profile.DefaultProfile;
import org.eclipse.paho.client.mqttv3.internal.websocket.Base64;

import java.util.List;

/**
 * 阿里云物联网平台工具类
 *
 * @author BoWenWang
 */
public class AliYunIotUtil {
    // 身份属性
    private static final String accessKey = "LTAI4Fe8hRhqfaexzT1rSKKR";
    private static final String accessSecret = "JaYTp10VuRC7k8BacfFW4ZEnAuhspZ";
    private static final String uid = "1295935431193801";
    private static final String regionId = "cn-shanghai";
    private static final String endPoint = "https://" + uid + ".iot-as-http2." + regionId + ".aliyuncs.com";

    /**
     * 设备内部类
     */
    static class Device {
        private String productKey;
        private String deviceName;

        public Device(String productKey, String deviceName) {
            this.productKey = productKey;
            this.deviceName = deviceName;
        }
    }

    /**
     * 开启设备监听
     * @param configurations
     */
    public static void init(List<AliYunIotUtil.Device> configurations) {
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
        for (Device device : configurations) {
            client.setMessageListener("/sys/" + device.productKey + "/" + device.deviceName
                    + "/thing/event/user/property/post", messageCallback);
        }

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

    /**
     * 发送指令给设备
     *
     * @param productKey 产品ID
     * @param deviceName 设备名字
     * @param cmd        指令
     */
    public static void publish(String productKey, String deviceName, String cmd) {
        //设置client的参数
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKey, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        // 发布
        PubRequest request = new PubRequest();
        request.setQos(0);
        //设置发布消息的topic
        request.setTopicFullName("/" + productKey + "/" + deviceName + "/user/get");
        request.setProductKey(productKey);
        //设置消息的内容，一定要用base64编码，否则乱码
        request.setMessageContent(Base64.encode(cmd));
        try {
            // 发布消息
            PubResponse response = client.getAcsResponse(request);
            System.out.println("pub success?:" + response.getSuccess() + "【到时候改成日志】");
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}