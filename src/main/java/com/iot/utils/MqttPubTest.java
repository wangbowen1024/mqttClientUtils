package com.iot.utils;

import java.util.Scanner;

/**
 * MQTT发布测试类
 * @author BoWenWang
 */
public class MqttPubTest {
    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();
            MqttUtil.publish("/nbiot/intelligentGranary/get", msg);
        }
    }
}
