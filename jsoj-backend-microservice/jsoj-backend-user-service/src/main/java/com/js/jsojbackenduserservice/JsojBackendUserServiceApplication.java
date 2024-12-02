package com.js.jsojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
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
 * @time 2024-11-04 03:14:45
 */
@SpringBootApplication
@MapperScan("com.js.jsojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.js")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.js.jsojbackendserviceclient.service"})
public class JsojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsojBackendUserServiceApplication.class, args);
    }

}
