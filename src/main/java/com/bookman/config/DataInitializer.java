package com.bookman.config;

import com.bookman.user.entity.User;
import com.bookman.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Virtual Thread 정보 로깅
        logVirtualThreadInfo();
        
        // 관리자 계정 생성
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@bookman.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("관리자")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            // createdAt은 @PrePersist에서 자동 설정됨
            userRepository.save(admin);
            log.info("✅ 관리자 계정이 생성되었습니다: admin/admin123");
        }

        // 테스트 사용자 계정 생성
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@bookman.com")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("일반사용자")
                    .role(User.Role.USER)
                    .isActive(true)
                    .build();
            // createdAt은 @PrePersist에서 자동 설정됨
            userRepository.save(user);
            log.info("✅ 테스트 사용자 계정이 생성되었습니다: user/user123");
        }
        
        log.info("🎉 BookMan 애플리케이션 초기화 완료!");
    }
    
    private void logVirtualThreadInfo() {
        String javaVersion = System.getProperty("java.version");
        String jvmName = System.getProperty("java.vm.name");
        
        log.info("☕ Java 버전: {} ({})", javaVersion, jvmName);
        
        // Virtual Thread 지원 여부 확인
        try {
            Thread virtualThread = Thread.ofVirtual().unstarted(() -> {});
            boolean isVirtualThreadSupported = virtualThread.isVirtual();
            
            if (isVirtualThreadSupported) {
                log.info("🚀 Virtual Thread가 활성화되었습니다!");
                log.info("   - 높은 동시성 처리 가능");
                log.info("   - 효율적인 I/O 작업 처리");
                log.info("   - 메모리 사용량 최적화");
                log.info("   - 성능 테스트 API: /api/performance/thread-info");
            } else {
                log.warn("⚠️ Virtual Thread를 사용할 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Virtual Thread 확인 중 오류: {}", e.getMessage());
        }
    }
}
