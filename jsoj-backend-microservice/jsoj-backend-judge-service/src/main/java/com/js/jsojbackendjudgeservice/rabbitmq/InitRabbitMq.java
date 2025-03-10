package com.js.jsojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-11-05 02:23:47
 */
@Slf4j
public class InitRabbitMq {

    public static void doInitCodeMq() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列,随机分配一个队列名称
            String codeQueue = "code_queue";
            channel.queueDeclare(codeQueue, true, false, false, null);
            channel.queueBind(codeQueue, EXCHANGE_NAME, "routingKey");
            log.info("消息队列启动成功！");
        } catch (Exception e) {
            log.error("消息队列启动失败");
            e.printStackTrace();
        }
    }
    // public static void main(String[] args) {
    //     doInitCodeMq();
    // }
}
