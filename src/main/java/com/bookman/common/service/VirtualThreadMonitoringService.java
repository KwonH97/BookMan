package com.bookman.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@ConditionalOnProperty(
    value = "spring.threads.virtual.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class VirtualThreadMonitoringService {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final AtomicLong virtualThreadCount = new AtomicLong(0);
    private final AtomicLong asyncTaskCount = new AtomicLong(0);

    /**
     * 비동기 작업 카운터 증가
     */
    public void incrementAsyncTaskCount() {
        asyncTaskCount.incrementAndGet();
    }

    /**
     * Virtual Thread 정보 주기적 모니터링 (30초마다)
     */
    @Scheduled(fixedDelay = 30000)
    public void monitorVirtualThreads() {
        long totalThreads = threadMXBean.getThreadCount();
        long peakThreads = threadMXBean.getPeakThreadCount();
        long currentAsyncTasks = asyncTaskCount.get();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        log.info("📊 [{}] Virtual Thread 모니터링 - 총 스레드: {}, 최대 스레드: {}, 비동기 작업: {}", 
                timestamp, totalThreads, peakThreads, currentAsyncTasks);
    }

    /**
     * 시스템 메모리 사용량 모니터링
     */
    @Scheduled(fixedDelay = 60000) // 1분마다
    public void monitorMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024);   // MB
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);     // MB
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        log.info("💾 [{}] 메모리 사용량 - 사용: {}MB, 전체: {}MB, 최대: {}MB, 사용률: {}%", 
                timestamp, usedMemory, totalMemory, maxMemory, 
                String.format("%.1f", (double) usedMemory / totalMemory * 100));
    }

    /**
     * 비동기 작업 시뮬레이션 (테스트용)
     */
    @Async("virtualThreadTaskExecutor")
    public void simulateAsyncWork(String taskName, long duration) {
        incrementAsyncTaskCount();
        
        String threadInfo = Thread.currentThread().toString();
        boolean isVirtual = Thread.currentThread().isVirtual();
        
        log.debug("🔄 [{}] 시작 - Virtual Thread: {}, Thread: {}", 
                taskName, isVirtual, threadInfo);
        
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("⚠️ 작업 중단: {}", taskName);
        }
        
        log.debug("✅ [{}] 완료 - 소요시간: {}ms", taskName, duration);
    }

    /**
     * 현재 스레드 정보 반환
     */
    public String getCurrentThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return String.format("Thread: %s, Virtual: %s, ID: %d", 
                currentThread.getName(), 
                currentThread.isVirtual(), 
                currentThread.threadId());
    }
}
