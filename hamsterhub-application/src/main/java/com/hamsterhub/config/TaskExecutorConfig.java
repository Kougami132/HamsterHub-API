package com.hamsterhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class TaskExecutorConfig {
    @Bean("downloadTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // 设置核心线程数
        executor.setMaxPoolSize(3); // 设置最大线程数
        executor.setQueueCapacity(10); // 设置队列容量
        executor.setThreadNamePrefix("Download-");
        executor.initialize();
        return executor;
    }
}
