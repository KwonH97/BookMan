package com.bookman.common;

import com.bookman.auth.dto.LoginRequest;
import com.bookman.auth.dto.RegisterRequest;
import com.bookman.user.entity.User;

/**
 * 테스트 데이터를 생성하는 팩토리 클래스
 */
public class TestDataFactory {

    public static RegisterRequest createValidRegisterRequest() {
        return RegisterRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .fullName("새로운 사용자")
                .build();
    }

    public static RegisterRequest createInvalidRegisterRequest() {
        return RegisterRequest.builder()
                .username("ab") // 너무 짧음
                .email("invalid-email") // 잘못된 이메일 형식
                .password("123") // 너무 짧음
                .fullName("") // 비어있음
                .build();
    }

    public static LoginRequest createValidLoginRequest() {
        return LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();
    }

    public static LoginRequest createInvalidLoginRequest() {
        return LoginRequest.builder()
                .username("wronguser")
                .password("wrongpassword")
                .build();
    }

    public static User createTestUser() {
        return User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .password("encodedPassword")
                .fullName("테스트 사용자")
                .role(User.Role.USER)
                .isActive(true)
                .build();
        // createdAt은 @PrePersist에서 자동 설정됨
    }

    public static User createTestAdmin() {
        return User.builder()
                .username("testadmin")
                .email("testadmin@test.com")
                .password("encodedPassword")
                .fullName("테스트 관리자")
                .role(User.Role.ADMIN)
                .isActive(true)
                .build();
        // createdAt은 @PrePersist에서 자동 설정됨
    }
}
