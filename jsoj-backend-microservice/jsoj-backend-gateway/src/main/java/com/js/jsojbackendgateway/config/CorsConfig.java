package com.js.jsojbackendgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 网关处理跨域
 * @author sakisaki
 * @date 2025/2/22 14:53
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 设置允许的 HTTP 方法
        config.addAllowedMethod("*");
        // 设置允许的请求头
        config.addAllowedHeader("*");
        // 设置是否允许发送凭据
        config.setAllowCredentials(true);
        // 设置线上前端项目地址,设置允许的源模式
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:8080", "http://127.0.0.1:8080"));
        // 注册 CORS 配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 将 CORS 配置应用到所有路径
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
