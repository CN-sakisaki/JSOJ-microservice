package com.js.jsojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * gateway启动类
 * @author sakisaki
 * @date 2025/2/22 14:53
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class JsojBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsojBackendGatewayApplication.class, args);
    }

}
