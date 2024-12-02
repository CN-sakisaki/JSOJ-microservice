package com.js.jsojbackendquestionservice.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息队列生产者
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-11-05 02:34:06
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 生产者发送消息
     *
     * @param exchange   发送消息到指定交换机
     * @param routingKey 消息标识
     * @param message    消息内容
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
