package com.bookman.user.controller;

import com.bookman.common.exception.ErrorResponse;
import com.bookman.user.dto.UserResponse;
import com.bookman.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
    name = "👤 User Management",
    description = """
        ## 사용자 관리 API
        
        인증된 사용자의 정보 조회 및 관리 기능을 제공합니다.
        
        ### 🔒 인증 요구사항
        - 모든 API는 JWT 토큰 인증이 필요합니다
        - 토큰은 Authorization 헤더에 `Bearer {token}` 형식으로 전달
        
        ### 👥 권한 시스템
        - **USER**: 일반 사용자 권한
        - **ADMIN**: 관리자 권한 (모든 기능 접근 가능)
        
        ### 🚀 사용 방법
        1. `/api/auth/login`으로 토큰 획득
        2. 우측 상단 **Authorize** 버튼에 토큰 입력
        3. API 호출 테스트
        """
)
@SecurityRequirement(name = "JWT")
public class UserController {

    @GetMapping("/me")
    @Operation(
        summary = "👤 내 정보 조회",
        description = """
            현재 로그인한 사용자의 상세 정보를 조회합니다.
            
            **📋 조회 정보:**
            - 기본 정보: 사용자ID, 사용자명, 이메일, 이름
            - 권한 정보: 역할(USER/ADMIN), 활성 상태
            - 시스템 정보: 계정 생성일
            
            **🔐 권한:** 로그인한 모든 사용자
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 조회 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponse.class),
                examples = {
                    @ExampleObject(
                        name = "일반 사용자",
                        value = """
                        {
                            "userId": 1,
                            "username": "user",
                            "email": "user@bookman.com",
                            "fullName": "일반사용자",
                            "role": "USER",
                            "isActive": true,
                            "createdAt": "2025-06-05T10:30:00"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "관리자",
                        value = """
                        {
                            "userId": 2,
                            "username": "admin",
                            "email": "admin@bookman.com",
                            "fullName": "관리자",
                            "role": "ADMIN",
                            "isActive": true,
                            "createdAt": "2025-06-05T10:30:00"
                        }
                        """
                    )
                }
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
                        "message": "인증이 필요합니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        UserResponse response = UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-only")
    @Operation(
        summary = "🔒 관리자 전용 테스트",
        description = """
            관리자만 접근 가능한 테스트 엔드포인트입니다.
            
            **🎯 목적:**
            - 관리자 권한 테스트
            - 권한 기반 접근 제어 확인
            - 시스템 보안 검증
            
            **🔐 권한:** ADMIN 역할만 접근 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 접근 성공 (관리자)",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = "관리자만 볼 수 있는 내용입니다."
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ 인증 실패",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "❌ 권한 부족 (일반 사용자)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "권한 부족",
                    value = """
                    {
                        "timestamp": "2025-06-05T10:30:00",
                        "status": 403,
                        "error": "Forbidden",
                        "message": "접근 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("관리자만 볼 수 있는 내용입니다.");
    }

    @GetMapping("/user-only")
    @Operation(
        summary = "👥 사용자 전용 테스트",
        description = """
            일반 사용자만 접근 가능한 테스트 엔드포인트입니다.
            
            **🎯 목적:**
            - 일반 사용자 권한 테스트
            - 역할별 접근 제어 확인
            
            **🔐 권한:** USER 역할만 접근 가능 (관리자 접근 불가)
            
            **💡 참고:** 이는 데모용으로, 실제로는 관리자가 모든 권한을 가지는 것이 일반적입니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 접근 성공 (일반 사용자)",
            content = @Content(
                mediaType = MediaType.TEXT_PLAIN_VALUE,
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = "일반 사용자만 볼 수 있는 내용입니다."
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ 인증 실패",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "❌ 권한 부족 (관리자)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> userOnly() {
        return ResponseEntity.ok("일반 사용자만 볼 수 있는 내용입니다.");
    }
}
