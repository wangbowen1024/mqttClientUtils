package com.aliyun.iot.demo;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.iot.model.v20180120.PubRequest;
import com.aliyuncs.iot.model.v20180120.PubResponse;
import com.aliyuncs.profile.DefaultProfile;
import org.eclipse.paho.client.mqttv3.internal.websocket.Base64;

/**
 * 设备数据下发--服务端
 */
public class PopPubServer {

    public static void main(String[] args) {
        String regionId = "cn-shanghai";
        String accessKey = "LTAI4Fe8hRhqfaexzT1rSKKR";
        String accessSecret = "JaYTp10VuRC7k8BacfFW4ZEnAuhspZ";
        final String productKey = "a1i6fHay1uY";
        final String deviceName = "BC28TEST";
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
        request.setMessageContent(Base64.encode("{\"accuracy\":0.001,\"time\":now}"));
        try {
            // 发布消息
            PubResponse response = client.getAcsResponse(request);
            System.out.println("pub success?:" + response.getSuccess());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
