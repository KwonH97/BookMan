package com.bookman.auth.controller;

import com.bookman.auth.dto.AuthResponse;
import com.bookman.auth.dto.LoginRequest;
import com.bookman.auth.dto.RegisterRequest;
import com.bookman.auth.service.AuthService;
import com.bookman.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "🔐 Authentication", 
    description = """
        ## 인증 및 권한 관리 API
        
        사용자 회원가입, 로그인, 토큰 검증 등의 인증 관련 기능을 제공합니다.
        
        ### 📋 주요 기능
        - 🆕 **회원가입**: 새로운 사용자 계정 생성
        - 🔑 **로그인**: JWT 토큰 기반 인증
        - ✅ **토큰 검증**: 현재 토큰의 유효성 확인
        
        ### 🚀 사용 가이드
        1. 회원가입 또는 로그인으로 JWT 토큰 획득
        2. 이후 모든 API 요청 시 `Authorization: Bearer {token}` 헤더 포함
        """
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "🆕 회원가입",
        description = """
            새로운 사용자 계정을 생성합니다.
            
            **📝 입력 검증:**
            - 사용자명: 3-50자, 중복 불가
            - 이메일: 유효한 형식, 중복 불가
            - 비밀번호: 최소 6자
            - 이름: 최대 100자
            
            **✨ 성공 시 JWT 토큰이 자동으로 발급됩니다.**
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 회원가입 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "username": "newuser",
                        "email": "newuser@example.com",
                        "fullName": "새로운 사용자",
                        "role": "USER"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ 입력값 검증 실패 또는 중복 데이터",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "검증 실패",
                        value = """
                        {
                            "timestamp": "2025-06-05T10:30:00",
                            "status": 400,
                            "error": "Validation Failed",
                            "message": "입력값 검증에 실패했습니다.",
                            "validationErrors": {
                                "username": "사용자명은 3-50자 사이여야 합니다",
                                "email": "올바른 이메일 형식이 아닙니다"
                            }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "중복 사용자명",
                        value = """
                        {
                            "timestamp": "2025-06-05T10:30:00",
                            "status": 400,
                            "error": "Bad Request",
                            "message": "이미 존재하는 사용자명입니다: newuser"
                        }
                        """
                    )
                }
            )
        )
    })
    public ResponseEntity<AuthResponse> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 정보",
            required = true,
            content = @Content(
                examples = @ExampleObject(
                    name = "회원가입 요청 예시",
                    value = """
                    {
                        "username": "newuser",
                        "email": "newuser@example.com",
                        "password": "password123",
                        "fullName": "새로운 사용자"
                    }
                    """
                )
            )
        )
        @Valid @RequestBody RegisterRequest request
    ) {
        log.info("회원가입 요청: {}", request.getUsername());
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "🔑 로그인",
        description = """
            사용자 인증 후 JWT 토큰을 발급합니다.
            
            **🔐 인증 방식:**
            - 사용자명과 비밀번호로 인증
            - 성공 시 24시간 유효한 JWT 토큰 발급
            
            **👥 테스트 계정:**
            - 관리자: `admin` / `admin123`
            - 일반사용자: `user` / `user123`
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 로그인 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "type": "Bearer",
                        "username": "admin",
                        "email": "admin@bookman.com",
                        "fullName": "관리자",
                        "role": "ADMIN"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ 인증 실패",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "timestamp": "2025-06-05T10:30:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "아이디 또는 비밀번호가 잘못되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ 입력값 검증 실패",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정보",
            required = true,
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "관리자 로그인",
                        value = """
                        {
                            "username": "admin",
                            "password": "admin123"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "일반사용자 로그인",
                        value = """
                        {
                            "username": "user",
                            "password": "user123"
                        }
                        """
                    )
                }
            )
        )
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("로그인 요청: {}", request.getUsername());
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("로그인 실패: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/validate")
    @Operation(
        summary = "✅ 토큰 검증",
        description = """
            현재 JWT 토큰의 유효성을 검증합니다.
            
            **🔒 인증 필요:**
            - Authorization 헤더에 Bearer 토큰 필요
            - 유효한 토큰인 경우에만 접근 가능
            
            **💡 사용 목적:**
            - 클라이언트에서 토큰 유효성 확인
            - 자동 로그아웃 구현
            - API 상태 확인
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 토큰 유효",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = "토큰이 유효합니다."
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ 토큰 무효 또는 미제공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                    {
                        "timestamp": "2025-06-05T10:30:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "인증이 필요합니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("토큰이 유효합니다.");
    }
}
