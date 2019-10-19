# MQTT JAVA客户端工具类
## 使用方法
右键需要运行的类，run main()

## 阿里云物联网平台MQTT客户端
* 在工具类AliYunIotUtil中填写好设备属性
* init方法：启动监听（订阅主题）
* publish方法：下发信息（发布主题）

## 自定义MQTT服务器客户端
* 自定义MQTT类继承AbstractMqttClient抽象类
* 实现自定义处理消息回调方法processMessage
* 方法解析（可调用函数）
    * init：根据参数初始化MQTT客户端
    * publish：发布主题
    * subscribe：订阅主题（每次订阅成功，会缓存主题信息到topicMap）
    * unsubscribe：取消订阅主题
    * unsubscribeAll：取消所有订阅主题
    * disconnect：断开连接
    * close：关闭客户端
    
## 自定义简易版MQTT服务器（发布、订阅分离）
* 在com.iot.utils包下
