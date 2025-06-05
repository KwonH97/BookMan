package com.bookman.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@Slf4j
public class VirtualThreadConfig {

    /**
     * Virtual Thread 기반 TaskExecutor 설정
     * JDK 21의 Virtual Thread를 사용하여 높은 동시성 처리
     */
    @Bean("virtualThreadTaskExecutor")
    @ConditionalOnProperty(
        value = "spring.threads.virtual.enabled", 
        havingValue = "true", 
        matchIfMissing = false
    )
    public AsyncTaskExecutor applicationTaskExecutor() {
        log.info("🚀 Virtual Thread TaskExecutor가 활성화되었습니다!");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Tomcat에서 Virtual Thread 사용 설정
     */
    @Bean
    @ConditionalOnProperty(
        value = "spring.threads.virtual.enabled", 
        havingValue = "true", 
        matchIfMissing = false
    )
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        log.info("🔧 Tomcat Virtual Thread 설정이 적용되었습니다!");
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    /**
     * 스케줄링 작업을 위한 Virtual Thread Executor
     */
    @Bean("scheduledVirtualThreadExecutor")
    @ConditionalOnProperty(
        value = "spring.threads.virtual.enabled", 
        havingValue = "true", 
        matchIfMissing = false
    )
    public AsyncTaskExecutor scheduledTaskExecutor() {
        log.info("📅 스케줄링용 Virtual Thread Executor가 설정되었습니다!");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
