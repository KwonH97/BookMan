package com.bookman.auth.service;

import com.bookman.auth.dto.AuthResponse;
import com.bookman.auth.dto.LoginRequest;
import com.bookman.auth.dto.RegisterRequest;
import com.bookman.auth.util.JwtUtil;
import com.bookman.common.service.VirtualThreadMonitoringService;
import com.bookman.user.entity.User;
import com.bookman.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final VirtualThreadMonitoringService monitoringService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String threadInfo = monitoringService.getCurrentThreadInfo();
        log.info("🆕 회원가입 처리 시작 - {}", threadInfo);
        
        // 중복 체크를 비동기로 처리
        validateUserUniqueness(request.getUsername(), request.getEmail());

        // 사용자 생성
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .isActive(true)
                .build();
        // createdAt은 @PrePersist에서 자동 설정됨

        User savedUser = userRepository.save(user);
        log.info("✅ 새 사용자 등록됨: {} - {}", savedUser.getUsername(), threadInfo);

        // JWT 토큰 생성을 비동기로 처리
        String token = generateTokenAsync(savedUser);

        // 비동기 작업 로깅
        logUserActivity(savedUser.getUsername(), "REGISTER");

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String threadInfo = monitoringService.getCurrentThreadInfo();
        log.info("🔑 로그인 처리 시작 - {} - {}", request.getUsername(), threadInfo);
        
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // JWT 토큰 생성을 비동기로 처리
        String token = generateTokenAsync(user);
        log.info("✅ 사용자 로그인: {} - {}", user.getUsername(), threadInfo);

        // 비동기 작업 로깅
        logUserActivity(user.getUsername(), "LOGIN");

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * 사용자 유니크성 검증 (Virtual Thread 활용)
     */
    private void validateUserUniqueness(String username, String email) {
        CompletableFuture<Boolean> usernameCheck = CompletableFuture.supplyAsync(() -> {
            boolean exists = userRepository.existsByUsername(username);
            log.debug("🔍 사용자명 중복 검사: {} - 결과: {}", username, exists);
            return exists;
        });

        CompletableFuture<Boolean> emailCheck = CompletableFuture.supplyAsync(() -> {
            boolean exists = userRepository.existsByEmail(email);
            log.debug("📧 이메일 중복 검사: {} - 결과: {}", email, exists);
            return exists;
        });

        // 두 검사가 모두 완료될 때까지 대기
        CompletableFuture.allOf(usernameCheck, emailCheck).join();

        if (usernameCheck.join()) {
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + username);
        }
        if (emailCheck.join()) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + email);
        }
    }

    /**
     * JWT 토큰 생성 (Virtual Thread 활용)
     */
    private String generateTokenAsync(User user) {
        return CompletableFuture.supplyAsync(() -> {
            String token = jwtUtil.generateToken(user);
            log.debug("🎫 JWT 토큰 생성 완료: {} characters", token.length());
            return token;
        }).join();
    }

    /**
     * 사용자 활동 로깅 (비동기 처리)
     */
    @Async("virtualThreadTaskExecutor")
    public void logUserActivity(String username, String action) {
        String threadInfo = monitoringService.getCurrentThreadInfo();
        
        // 시뮬레이션: 복잡한 로깅 작업
        try {
            Thread.sleep(100); // 외부 로깅 시스템 호출 시뮬레이션
            
            log.info("📝 사용자 활동 로그 - 사용자: {}, 액션: {}, 스레드: {}", 
                    username, action, threadInfo);
            
            monitoringService.incrementAsyncTaskCount();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("⚠️ 로깅 작업 중단: {}", username);
        }
    }
}
