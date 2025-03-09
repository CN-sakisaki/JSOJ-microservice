package com.js.jsojbackendjudgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 *
 * @author sakisaki
 * @date 2025/3/9 20:55
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("judgeThreadPool")
    public ExecutorService judgeThreadPool() {
        return new ThreadPoolExecutor(
                // 核心线程数
                5,
                // 最大线程数
                10,
                // 空闲线程存活时间
                60L, TimeUnit.SECONDS,
                // 任务队列容量
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                // 拒绝策略
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
