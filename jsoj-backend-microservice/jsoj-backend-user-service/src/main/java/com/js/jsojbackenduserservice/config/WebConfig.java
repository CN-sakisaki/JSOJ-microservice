package com.js.jsojbackenduserservice.config;

import com.js.jsojbackenduserservice.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author sakisaki
 * @date 2025/2/23 23:22
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;

    @Autowired
    public WebConfig(@Lazy UserInterceptor userInterceptor) {
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(userInterceptor)
                // 拦截所有API请求
                .addPathPatterns("/api/**")
                // 排除登录请求
                .excludePathPatterns("/api/user/login")
                // 排除注册请求
                .excludePathPatterns("/api/user/register")
                // 排除监控端点
                .excludePathPatterns("/actuator/**");
    }
}
