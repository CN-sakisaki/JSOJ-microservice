package com.js.jsojbackendjudgeservice.rabbitmq;

import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.exception.BusinessException;
import com.js.jsojbackendcommon.exception.ThrowUtils;
import com.js.jsojbackendjudgeservice.judge.JudgeService;
import com.js.jsojbackendmodel.entity.QuestionSubmit;
import com.js.jsojbackendmodel.enums.QuestionStatusEnum;
import com.js.jsojbackendserviceclient.service.QuestionFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 消息队列消费者
 *
 * @author JianShang
 * @version 1.0.0
 * @time 2024-11-05 02:37:30
 */
@Component
@Slf4j
public class QuestionSubmitMessageConsumer {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionFeignClient questionFeignClient;

    /**
     * 指定程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("Received message: {}", message);
        try {
            ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.PARAMS_ERROR, "Empty message");
            long questionSubmitId = Long.parseLong(message);
            // 幂等性检查：判断是否已处理过该消息
            QuestionSubmit submit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            if (!Objects.equals(submit.getStatus(), QuestionStatusEnum.WAITING.getValue())) {
                log.warn("Duplicate message detected: {}", questionSubmitId);
                // 已处理则直接ACK
                channel.basicAck(deliveryTag, false);
                return;
            }

            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false);
        } catch (BusinessException e) {
            log.error("Business error: {}", e.getMessage());
            // 不重试（进入死信队列）
            channel.basicReject(deliveryTag, false);
        } catch (Exception e) {
            log.error("System error: ", e);
            // 允许重试
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
