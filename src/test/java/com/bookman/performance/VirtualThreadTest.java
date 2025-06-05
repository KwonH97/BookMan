package com.bookman.performance;

import com.bookman.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
    "spring.threads.virtual.enabled=true"
})
@EnabledForJreRange(min = JRE.JAVA_21)
@DisplayName("Virtual Thread 성능 테스트")
class VirtualThreadTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("Virtual Thread 기본 기능 테스트")
    class BasicVirtualThreadTest {

        @Test
        @DisplayName("Virtual Thread 생성 및 실행 테스트")
        void createAndRunVirtualThread() {
            // Given
            final boolean[] taskCompleted = {false};
            
            // When
            Thread virtualThread = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(100);
                    taskCompleted[0] = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Then
            assertThat(virtualThread.isVirtual()).isTrue();
            
            try {
                virtualThread.join();
                assertThat(taskCompleted[0]).isTrue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("대량 Virtual Thread 생성 테스트")
        void createManyVirtualThreads() throws InterruptedException {
            // Given
            int threadCount = 1000;
            List<Thread> threads = new ArrayList<>();
            
            // When
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                Thread thread = Thread.ofVirtual().start(() -> {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                threads.add(thread);
            }
            
            // 모든 스레드 완료 대기
            for (Thread thread : threads) {
                thread.join();
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Then
            assertThat(threads).hasSize(threadCount);
            assertThat(totalTime).isLessThan(5000); // 5초 이내 완료
            
            // 모든 스레드가 Virtual Thread인지 확인
            threads.forEach(thread -> assertThat(thread.isVirtual()).isTrue());
        }

        @Test
        @DisplayName("CompletableFuture와 Virtual Thread 조합 테스트")
        void completableFutureWithVirtualThreads() {
            // Given
            int taskCount = 500;
            
            // When
            long startTime = System.currentTimeMillis();
            
            List<CompletableFuture<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                final int taskId = i;
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(50);
                        return taskId * 2;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -1;
                    }
                }, Executors.newVirtualThreadPerTaskExecutor());
                
                futures.add(future);
            }
            
            // 모든 작업 완료 대기
            List<Integer> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Then
            assertThat(results).hasSize(taskCount);
            assertThat(results).doesNotContain(-1); // 모든 작업 성공
            assertThat(totalTime).isLessThan(3000); // 3초 이내 완료
        }
    }

    @Nested
    @DisplayName("Virtual Thread API 테스트")
    class VirtualThreadApiTest {

        @Test
        @DisplayName("스레드 정보 조회 API 테스트")
        void getThreadInfo() throws Exception {
            // When
            mockMvc.perform(get("/api/performance/thread-info"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.isVirtual").value(true))
                    .andExpect(jsonPath("$.threadId").exists())
                    .andExpect(jsonPath("$.threadName").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.processingTime").exists());
        }

        @Test
        @DisplayName("Virtual Thread 스트레스 테스트 API")
        void stressTestApi() throws Exception {
            // When
            mockMvc.perform(post("/api/performance/stress-test")
                    .param("taskCount", "100")
                    .param("delayMs", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.taskCount").value(100))
                    .andExpect(jsonPath("$.successCount").value(100))
                    .andExpect(jsonPath("$.failureCount").value(0))
                    .andExpect(jsonPath("$.threadsUsed").value("Virtual Threads"))
                    .andExpect(jsonPath("$.totalTimeMs").exists())
                    .andExpect(jsonPath("$.averageTimeMs").exists());
        }

        @Test
        @DisplayName("스트레스 테스트 파라미터 검증")
        void stressTestParameterValidation() throws Exception {
            // When & Then - 작업 수 초과
            mockMvc.perform(post("/api/performance/stress-test")
                    .param("taskCount", "15000"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("작업 수는 10000개를 초과할 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("Virtual Thread 성능 비교 테스트")
    class PerformanceComparisonTest {

        @Test
        @DisplayName("Virtual Thread vs Platform Thread 처리량 비교")
        void compareVirtualThreadVsPlatformThread() throws InterruptedException {
            int taskCount = 1000;
            int delayMs = 50;
            
            // Virtual Thread 테스트
            long virtualThreadTime = measureExecutionTime(() -> {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int i = 0; i < taskCount; i++) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, Executors.newVirtualThreadPerTaskExecutor());
                    futures.add(future);
                }
                
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            });
            
            // Platform Thread 테스트 (제한된 스레드 풀)
            long platformThreadTime = measureExecutionTime(() -> {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                var executor = Executors.newFixedThreadPool(200); // 제한된 스레드 풀
                
                try {
                    for (int i = 0; i < taskCount; i++) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(delayMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }, executor);
                        futures.add(future);
                    }
                    
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                } finally {
                    executor.shutdown();
                }
            });
            
            // 결과 검증
            System.out.printf("📊 성능 비교 결과:%n");
            System.out.printf("   Virtual Thread: %d ms%n", virtualThreadTime);
            System.out.printf("   Platform Thread: %d ms%n", platformThreadTime);
            System.out.printf("   성능 향상: %.2fx%n", (double) platformThreadTime / virtualThreadTime);
            
            // Virtual Thread가 더 빠르거나 비슷해야 함
            assertThat(virtualThreadTime).isLessThanOrEqualTo(platformThreadTime * 2);
        }

        private long measureExecutionTime(Runnable task) {
            long startTime = System.currentTimeMillis();
            task.run();
            return System.currentTimeMillis() - startTime;
        }
    }
}
