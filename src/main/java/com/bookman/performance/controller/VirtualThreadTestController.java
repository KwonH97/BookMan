package com.bookman.performance.controller;

import com.bookman.common.service.VirtualThreadMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "⚡ Virtual Thread Performance", 
    description = """
        ## Virtual Thread 성능 테스트 API
        
        JDK 21의 Virtual Thread 기능을 테스트하고 성능을 측정할 수 있는 API들입니다.
        
        ### 🎯 주요 기능
        - 🚀 **대량 동시 요청 처리**: Virtual Thread의 동시성 성능 테스트
        - 📊 **스레드 모니터링**: 실시간 스레드 상태 확인
        - ⏱️ **응답 시간 측정**: 기존 Thread vs Virtual Thread 비교
        
        ### 💡 Virtual Thread 장점
        - **높은 동시성**: 수백만 개의 스레드 생성 가능
        - **낮은 메모리 사용량**: 기존 Thread 대비 훨씬 적은 메모리
        - **효율적 I/O 처리**: 블로킹 I/O에서도 높은 성능
        """
)
@ConditionalOnProperty(
    value = "spring.threads.virtual.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class VirtualThreadTestController {

    private final VirtualThreadMonitoringService monitoringService;

    @GetMapping("/thread-info")
    @Operation(
        summary = "🔍 현재 스레드 정보 조회",
        description = """
            현재 요청을 처리하는 스레드의 정보를 반환합니다.
            
            **📋 제공 정보:**
            - 스레드 이름 및 ID
            - Virtual Thread 여부
            - 처리 시간
            """
    )
    public ResponseEntity<Map<String, Object>> getThreadInfo() {
        long startTime = System.currentTimeMillis();
        
        Thread currentThread = Thread.currentThread();
        String threadInfo = monitoringService.getCurrentThreadInfo();
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = Map.of(
            "threadInfo", threadInfo,
            "threadName", currentThread.getName(),
            "isVirtual", currentThread.isVirtual(),
            "threadId", currentThread.threadId(),
            "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "processingTime", processingTime + "ms"
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stress-test")
    @Operation(
        summary = "🔥 Virtual Thread 스트레스 테스트",
        description = """
            지정된 수의 동시 작업을 Virtual Thread로 처리하여 성능을 테스트합니다.
            
            **⚠️ 주의사항:**
            - 높은 동시성 테스트이므로 시스템 리소스를 모니터링하세요
            - 개발 환경에서만 사용을 권장합니다
            
            **📊 측정 항목:**
            - 총 처리 시간
            - 평균 작업 시간
            - 성공/실패 개수
            """
    )
    public ResponseEntity<Map<String, Object>> stressTest(
        @Parameter(description = "동시 실행할 작업 수 (1-10000)", example = "1000")
        @RequestParam(defaultValue = "1000") int taskCount,
        
        @Parameter(description = "각 작업의 지연 시간(ms)", example = "100") 
        @RequestParam(defaultValue = "100") int delayMs
    ) {
        if (taskCount > 10000) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "작업 수는 10000개를 초과할 수 없습니다.")
            );
        }
        
        long startTime = System.currentTimeMillis();
        
        log.info("🔥 Virtual Thread 스트레스 테스트 시작 - 작업 수: {}, 지연: {}ms", taskCount, delayMs);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // Virtual Thread로 대량의 비동기 작업 실행
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // I/O 작업 시뮬레이션
                    Thread.sleep(delayMs + ThreadLocalRandom.current().nextInt(50));
                    
                    // 간헐적으로 CPU 집약적 작업 시뮬레이션
                    if (taskId % 100 == 0) {
                        performCpuIntensiveTask();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("⚠️ 작업 중단: Task-{}", taskId);
                }
            });
            
            futures.add(future);
        }
        
        // 모든 작업 완료 대기
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allTasks.join();
        } catch (Exception e) {
            log.error("❌ 스트레스 테스트 중 오류 발생", e);
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        long successCount = futures.stream()
            .mapToLong(f -> f.isCompletedExceptionally() ? 0 : 1)
            .sum();
        long failureCount = taskCount - successCount;
        
        log.info("✅ Virtual Thread 스트레스 테스트 완료 - 총 시간: {}ms, 성공: {}, 실패: {}", 
                totalTime, successCount, failureCount);
        
        Map<String, Object> response = Map.of(
            "taskCount", taskCount,
            "totalTimeMs", totalTime,
            "averageTimeMs", (double) totalTime / taskCount,
            "successCount", successCount,
            "failureCount", failureCount,
            "threadsUsed", "Virtual Threads",
            "threadInfo", monitoringService.getCurrentThreadInfo(),
            "message", String.format("✅ %d개 작업이 %.3f초에 완료되었습니다!", 
                    taskCount, totalTime / 1000.0)
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * CPU 집약적 작업 시뮬레이션
     */
    private void performCpuIntensiveTask() {
        // 간단한 수학 계산으로 CPU 사용
        double result = 0;
        for (int i = 0; i < 10000; i++) {
            result += Math.sqrt(i) * Math.sin(i);
        }
        // 결과를 사용하여 최적화 방지
        if (result > Double.MAX_VALUE) {
            log.debug("Computation result: {}", result);
        }
    }
}
