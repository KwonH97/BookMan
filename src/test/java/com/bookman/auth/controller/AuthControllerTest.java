package com.bookman.auth.controller;

import com.bookman.auth.dto.LoginRequest;
import com.bookman.auth.dto.RegisterRequest;
import com.bookman.common.BaseIntegrationTest;
import com.bookman.common.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("인증 컨트롤러 테스트")
class AuthControllerTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("유효한 회원가입 요청시 성공해야 한다")
        void register_WithValidRequest_ShouldSucceed() throws Exception {
            // Given
            RegisterRequest request = TestDataFactory.createValidRegisterRequest();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.username", is(request.getUsername())))
                    .andExpect(jsonPath("$.email", is(request.getEmail())))
                    .andExpect(jsonPath("$.fullName", is(request.getFullName())))
                    .andExpect(jsonPath("$.role", is("USER")));
        }

        @Test
        @DisplayName("잘못된 회원가입 요청시 검증 오류가 발생해야 한다")
        void register_WithInvalidRequest_ShouldFailValidation() throws Exception {
            // Given
            RegisterRequest request = TestDataFactory.createInvalidRegisterRequest();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error", is("Validation Failed")))
                    .andExpect(jsonPath("$.validationErrors", notNullValue()));
        }

        @Test
        @DisplayName("중복된 사용자명으로 회원가입시 실패해야 한다")
        void register_WithDuplicateUsername_ShouldFail() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username(testUser.getUsername()) // 이미 존재하는 사용자명
                    .email("different@test.com")
                    .password("password123")
                    .fullName("다른 사용자")
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("이미 존재하는 사용자명")));
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입시 실패해야 한다")
        void register_WithDuplicateEmail_ShouldFail() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("differentuser")
                    .email(testUser.getEmail()) // 이미 존재하는 이메일
                    .password("password123")
                    .fullName("다른 사용자")
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("이미 존재하는 이메일")));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("유효한 로그인 요청시 성공해야 한다")
        void login_WithValidCredentials_ShouldSucceed() throws Exception {
            // Given
            LoginRequest request = TestDataFactory.createValidLoginRequest();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                    .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                    .andExpect(jsonPath("$.role", is("USER")));
        }

        @Test
        @DisplayName("잘못된 자격증명으로 로그인시 실패해야 한다")
        void login_WithInvalidCredentials_ShouldFail() throws Exception {
            // Given
            LoginRequest request = TestDataFactory.createInvalidLoginRequest();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", is("아이디 또는 비밀번호가 잘못되었습니다.")));
        }

        @Test
        @DisplayName("잘못된 로그인 요청 형식시 검증 오류가 발생해야 한다")
        void login_WithInvalidFormat_ShouldFailValidation() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("ab") // 너무 짧음
                    .password("123") // 너무 짧음
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)));

            // Then
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Validation Failed")));
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰으로 검증시 성공해야 한다")
        void validateToken_WithValidToken_ShouldSucceed() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/auth/validate")
                    .header("Authorization", "Bearer " + userToken));

            // Then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("토큰이 유효합니다."));
        }

        @Test
        @DisplayName("토큰 없이 검증시 실패해야 한다")
        void validateToken_WithoutToken_ShouldFail() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/auth/validate"));

            // Then
            result.andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 토큰으로 검증시 실패해야 한다")
        void validateToken_WithInvalidToken_ShouldFail() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/auth/validate")
                    .header("Authorization", "Bearer invalid-token"));

            // Then
            result.andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}
