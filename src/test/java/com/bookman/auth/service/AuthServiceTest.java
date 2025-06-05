package com.bookman.auth.service;

import com.bookman.auth.dto.AuthResponse;
import com.bookman.auth.dto.LoginRequest;
import com.bookman.auth.dto.RegisterRequest;
import com.bookman.auth.util.JwtUtil;
import com.bookman.common.TestDataFactory;
import com.bookman.user.entity.User;
import com.bookman.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        registerRequest = TestDataFactory.createValidRegisterRequest();
        loginRequest = TestDataFactory.createValidLoginRequest();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("유효한 회원가입 요청시 성공해야 한다")
        void register_WithValidRequest_ShouldSucceed() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getRole()).isEqualTo("USER");

            verify(userRepository).existsByUsername(registerRequest.getUsername());
            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(passwordEncoder).encode(registerRequest.getPassword());
            verify(userRepository).save(any(User.class));
            verify(jwtUtil).generateToken(any(User.class));
        }

        @Test
        @DisplayName("중복된 사용자명으로 회원가입시 예외가 발생해야 한다")
        void register_WithDuplicateUsername_ShouldThrowException() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이미 존재하는 사용자명");

            verify(userRepository).existsByUsername(registerRequest.getUsername());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입시 예외가 발생해야 한다")
        void register_WithDuplicateEmail_ShouldThrowException() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이미 존재하는 이메일");

            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("유효한 로그인 요청시 성공해야 한다")
        void login_WithValidCredentials_ShouldSucceed() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(testUser)).thenReturn("jwt-token");

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(response.getRole()).isEqualTo("USER");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUsername(testUser.getUsername());
            verify(jwtUtil).generateToken(testUser);
        }

        @Test
        @DisplayName("잘못된 자격증명으로 로그인시 예외가 발생해야 한다")
        void login_WithInvalidCredentials_ShouldThrowException() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByUsername(any());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("인증 후 사용자를 찾을 수 없으면 예외가 발생해야 한다")
        void login_WhenUserNotFoundAfterAuth_ShouldThrowException() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUsername(testUser.getUsername());
            verify(jwtUtil, never()).generateToken(any());
        }
    }
}
