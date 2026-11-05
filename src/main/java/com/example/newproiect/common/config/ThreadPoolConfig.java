package com.example.newproiect.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 批量用户查询专用线程池。
 *
 * 不和 Tomcat 请求线程共用，避免批量任务占满 Web 请求线程。
 */
@Configuration
@EnableConfigurationProperties(UserBatchThreadPoolProperties.class)
public class ThreadPoolConfig {

    @Bean(name = "userBatchExecutor")
    public ThreadPoolTaskExecutor userBatchExecutor(UserBatchThreadPoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程：平时保留的工作线程数量。
        executor.setCorePoolSize(properties.coreSize());

        // 最大线程：队列满后最多可以扩展到的线程数量。
        executor.setMaxPoolSize(properties.maxSize());

        // 队列容量：线程都在忙时，任务先进入这个有界队列。
        executor.setQueueCapacity(properties.queueCapacity());

        // 空闲线程超过这个时间后可以被回收。
        executor.setKeepAliveSeconds(Math.toIntExact(properties.keepAlive().toSeconds()));

        // 日志中可以通过 user-batch-1、user-batch-2 识别线程。
        executor.setThreadNamePrefix("user-batch-");

        // 队列也满了以后，由提交任务的线程自己执行，避免任务被静默丢弃。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 应用关闭时等待已提交任务结束。
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
