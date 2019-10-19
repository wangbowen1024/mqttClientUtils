package com.mqtt;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        String topic1 = "/test/topic1";
        String topic2 = "/test/topic2";
        String topic3 = "/test/topic3";
        MyMqttClient myMqttClient = new MyMqttClient("tcp://xxx.xxx.xxx.xxx:1883", "MyMqttUtil", "test", "test");
        myMqttClient.subscribe(topic1);
        myMqttClient.subscribe(topic2);
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();
            if ("resub".equals(s)) {
                myMqttClient.subscribe(topic1);
                myMqttClient.subscribe(topic2);
                System.out.println(myMqttClient.getTopicMap().size());
            } else if ("c".equals(s)) {
                myMqttClient.unsubscribe(topic1);
            } else if ("clean".equals(s)) {
                myMqttClient.unsubscribeAll();
            } else {
                myMqttClient.publish(topic3, s);
            }
        }

    }
}
