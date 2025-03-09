package com.js.jsojbackendquestionservice.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
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
@Slf4j
public class QuestionSubmitMessageProducer {
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
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        } catch (Exception e) {
            log.error("消息发送失败: {}", e.getMessage());
            throw new RuntimeException("消息发送失败", e);
        }
    }
}
