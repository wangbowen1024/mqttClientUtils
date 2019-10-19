package com.iot.utils;

import java.util.Scanner;

/**
 * MQTT发布订阅测试类
 */
public class MqttSubTest {
    public static void main(String[] args) {

        MqttUtil.subscribe("/nbiot/intelligentGranary/post");

    }
}
