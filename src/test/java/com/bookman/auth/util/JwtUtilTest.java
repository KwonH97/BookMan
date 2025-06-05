package com.bookman.auth.util;

import com.bookman.auth.config.JwtConfig;
import com.bookman.common.TestDataFactory;
import com.bookman.user.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 유틸리티 테스트")
class JwtUtilTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecret()).thenReturn("testSecretKey1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        when(jwtConfig.getExpiration()).thenReturn(3600000L); // 1시간

        jwtUtil = new JwtUtil(jwtConfig);
        testUser = TestDataFactory.createTestUser();
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class GenerateTokenTest {

        @Test
        @DisplayName("유효한 사용자로 토큰 생성시 성공해야 한다")
        void generateToken_WithValidUser_ShouldSucceed() {
            // When
            String token = jwtUtil.generateToken(testUser);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
        }

        @Test
        @DisplayName("생성된 토큰에서 사용자명을 추출할 수 있어야 한다")
        void generateToken_ShouldExtractUsername() {
            // When
            String token = jwtUtil.generateToken(testUser);
            String extractedUsername = jwtUtil.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("생성된 토큰이 아직 만료되지 않았어야 한다")
        void generateToken_ShouldNotBeExpired() {
            // When
            String token = jwtUtil.generateToken(testUser);
            Date expiration = jwtUtil.extractExpiration(token);

            // Then
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰과 올바른 사용자로 검증시 성공해야 한다")
        void validateToken_WithValidTokenAndCorrectUser_ShouldReturnTrue() {
            // Given
            String token = jwtUtil.generateToken(testUser);

            // When
            Boolean isValid = jwtUtil.validateToken(token, testUser);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("유효한 토큰이지만 다른 사용자로 검증시 실패해야 한다")
        void validateToken_WithValidTokenButWrongUser_ShouldReturnFalse() {
            // Given
            String token = jwtUtil.generateToken(testUser);
            User otherUser = TestDataFactory.createTestAdmin();

            // When
            Boolean isValid = jwtUtil.validateToken(token, otherUser);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("잘못된 토큰으로 검증시 실패해야 한다")
        void validateToken_WithInvalidToken_ShouldReturnFalse() {
            // Given
            String invalidToken = "invalid.token.here";

            // When
            Boolean isValid = jwtUtil.validateToken(invalidToken, testUser);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 유효성 검사 테스트")
    class IsTokenValidTest {

        @Test
        @DisplayName("유효한 토큰은 valid해야 한다")
        void isTokenValid_WithValidToken_ShouldReturnTrue() {
            // Given
            String token = jwtUtil.generateToken(testUser);

            // When
            Boolean isValid = jwtUtil.isTokenValid(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("잘못된 토큰은 invalid해야 한다")
        void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
            // Given
            String invalidToken = "invalid.token.here";

            // When
            Boolean isValid = jwtUtil.isTokenValid(invalidToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰은 invalid해야 한다")
        void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
            // Given - 이미 만료된 토큰을 시뮬레이션하기 위해 과거 시간으로 설정
            when(jwtConfig.getExpiration()).thenReturn(-1000L); // 음수로 설정하여 즉시 만료
            JwtUtil expiredJwtUtil = new JwtUtil(jwtConfig);
            String expiredToken = expiredJwtUtil.generateToken(testUser);

            // When
            Boolean isValid = expiredJwtUtil.isTokenValid(expiredToken);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("클레임 추출 테스트")
    class ExtractClaimsTest {

        @Test
        @DisplayName("토큰에서 사용자명을 추출할 수 있어야 한다")
        void extractUsername_ShouldReturnCorrectUsername() {
            // Given
            String token = jwtUtil.generateToken(testUser);

            // When
            String username = jwtUtil.extractUsername(token);

            // Then
            assertThat(username).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("토큰에서 만료시간을 추출할 수 있어야 한다")
        void extractExpiration_ShouldReturnCorrectExpiration() {
            // Given
            String token = jwtUtil.generateToken(testUser);

            // When
            Date expiration = jwtUtil.extractExpiration(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("잘못된 토큰에서 클레임 추출시 예외가 발생해야 한다")
        void extractClaim_WithInvalidToken_ShouldThrowException() {
            // Given
            String invalidToken = "invalid.token.here";

            // When & Then
            assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                    .isInstanceOf(JwtException.class);
        }
    }
}
