package com.aliyun.iot.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * IotUtils测试类
 * @author BoWenWang
 */
public class AliYunIotUtilTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 设备信息
        final String productKey = "a1i6fHay1uY";
        final String deviceName = "BC28TEST";
        // 添加监听设备信息
        List<AliYunIotUtil.Device> list = new ArrayList<AliYunIotUtil.Device>();
        list.add(new AliYunIotUtil.Device(productKey, deviceName));
        // 开启监听
        AliYunIotUtil.init(list);
        while (true) {
            System.out.print("Cmd >> ");
            String cmd = scanner.nextLine();
            if ("exit".equals(cmd)) {
                return;
            }
            // 向指定设备发送指令
            AliYunIotUtil.publish(productKey, deviceName, cmd);
        }
    }
}
