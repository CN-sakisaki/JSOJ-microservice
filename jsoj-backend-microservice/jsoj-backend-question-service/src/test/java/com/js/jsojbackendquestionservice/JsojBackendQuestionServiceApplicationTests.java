package com.js.jsojbackendquestionservice;

import com.js.jsojbackendquestionservice.rabbitmq.QuestionSubmitMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class JsojBackendQuestionServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    private QuestionSubmitMessageProducer myMessageProducer;

    @Test
    void sendMessage() {
        myMessageProducer.sendMessage("code_exchange", "my_routingKey", "你好呀");
    }
}
