package com.js.jsojbackenduserservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * openAPI文档配置
 * @author sakisaki
 * @date 2025/2/28 17:25
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8102/api/user/v3/api-docs");
        devServer.setDescription("开发环境服务器");

        return new OpenAPI()
                .info(new Info()
                        .title("API 文档")
                        .version("1.0")
                        .description("接口说明"))
                .servers(Collections.singletonList(devServer));
    }
}
