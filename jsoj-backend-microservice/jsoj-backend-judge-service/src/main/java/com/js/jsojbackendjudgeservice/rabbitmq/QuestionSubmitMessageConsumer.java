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
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 消息队列消费者
 * 该类负责从 RabbitMQ 的 "code_queue" 队列中接收消息，并对消息进行处理，调用判题服务进行判题操作
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

    // 注入自定义的线程池，用于异步处理消息
    @Resource(name = "judgeThreadPool")
    private ExecutorService threadPool;

    /**
     * 接收 RabbitMQ 消息的方法
     *
     * @param message      接收到的消息内容，为题目提交记录的 ID
     * @param channel      RabbitMQ 通道，用于手动确认消息
     * @param deliveryTag  消息的投递标签，用于标识消息
     */
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 记录接收到的消息
        log.info("Received message: {}", message);
        try {
            // 进行参数校验，若消息为空，则抛出参数错误异常
            ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.PARAMS_ERROR, "Empty message");
            // 将消息转换为长整型的题目提交记录 ID
            long questionSubmitId = Long.parseLong(message);

            // 提交任务到线程池进行异步处理，避免阻塞消息接收
            threadPool.execute(() -> processMessage(questionSubmitId, channel, deliveryTag));

        } catch (Exception e) {
            // 处理消息接收过程中出现的通用错误
            handleGeneralError(channel, deliveryTag, e);
        }
    }

    /**
     * 处理消息的具体逻辑
     *
     * @param questionSubmitId 题目提交记录的 ID
     * @param channel          RabbitMQ 通道，用于手动确认消息
     * @param deliveryTag      消息的投递标签，用于标识消息
     */
    private void processMessage(long questionSubmitId, Channel channel, long deliveryTag) {
        try {
            // 进行幂等性检查，避免重复处理相同的消息
            QuestionSubmit submit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            // 若提交记录的状态不是等待状态，则认为是重复消息
            if (!Objects.equals(submit.getStatus(), QuestionStatusEnum.WAITING.getValue())) {
                log.warn("Duplicate message: {}", questionSubmitId);
                // 手动确认消息，告知 RabbitMQ 该消息已处理
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 调用判题服务进行判题操作
            judgeService.doJudge(questionSubmitId);

            // 判题完成后，手动确认消息，告知 RabbitMQ 该消息已处理
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 处理任务执行过程中出现的错误
            handleTaskError(channel, deliveryTag, e);
        }
    }

    /**
     * 处理任务执行过程中出现的错误
     *
     * @param channel      RabbitMQ 通道，用于手动确认消息
     * @param deliveryTag  消息的投递标签，用于标识消息
     * @param e            捕获到的异常
     */
    private void handleTaskError(Channel channel, long deliveryTag, Exception e) {
        try {
            // 若为业务异常，则记录错误信息并拒绝消息，不进行重试
            if (e instanceof BusinessException) {
                log.error("Business error: {}", e.getMessage());
                channel.basicReject(deliveryTag, false);
            } else {
                // 若为系统异常，则记录错误信息并进行消息否定确认，允许重试
                log.error("System error: ", e);
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException ex) {
            // 处理通道操作过程中出现的异常
            log.error("Channel error: ", ex);
        }
    }

    /**
     * 处理消息接收过程中出现的通用错误
     *
     * @param channel      RabbitMQ 通道，用于手动确认消息
     * @param deliveryTag  消息的投递标签，用于标识消息
     * @param e            捕获到的异常
     */
    private void handleGeneralError(Channel channel, long deliveryTag, Exception e) {
        try {
            // 记录消息处理过程中出现的错误信息
            log.error("Message processing error: ", e);
            // 进行消息否定确认，不进行重试
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException ex) {
            // 处理通道操作过程中出现的异常
            log.error("Channel error: ", ex);
        }
    }
}