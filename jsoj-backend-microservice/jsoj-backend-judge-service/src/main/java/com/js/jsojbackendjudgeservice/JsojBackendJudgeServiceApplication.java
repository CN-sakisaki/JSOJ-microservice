package com.js.jsojbackendjudgeservice;

import com.js.jsojbackendjudgeservice.rabbitmq.InitRabbitMq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author JianShang
 * @version 1.0.0
 * @time 2024-11-04 01:34:38
 */
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.js")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.js.jsojbackendserviceclient.service"})
public class JsojBackendJudgeServiceApplication {
    public static void main(String[] args) {
        InitRabbitMq.doInitCodeMq();
        SpringApplication.run(JsojBackendJudgeServiceApplication.class, args);
    }

}
