package com.example.newproiect.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 对应 application.yml 中的 app.thread-pool.user-batch 配置。
 */
@ConfigurationProperties(prefix = "app.thread-pool.user-batch")
public record UserBatchThreadPoolProperties(
        int coreSize,
        int maxSize,
        int queueCapacity,
        Duration keepAlive,
        Duration taskTimeout
) {
}
