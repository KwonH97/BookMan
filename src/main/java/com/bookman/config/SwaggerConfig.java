package com.bookman.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        // 서버 정보
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("로컬 개발 서버");

        // API 정보
        Info info = new Info()
                .title("📚 BookMan API")
                .description("""
                        ## 도서 관리 시스템 REST API
                        
                        ### 🎯 주요 기능
                        - **인증 및 권한 관리**: JWT 기반 로그인/회원가입
                        - **도서 관리**: CRUD 및 검색 기능
                        - **작가 관리**: 작가 정보 관리
                        - **사용자 관리**: 관리자/일반 사용자 구분
                        
                        ### 🔐 인증 방법
                        1. `/api/auth/login` 또는 `/api/auth/register`로 JWT 토큰 획득
                        2. 우측 상단 **Authorize** 버튼 클릭
                        3. `Bearer {토큰}` 형식으로 입력 (Bearer 키워드 포함)
                        
                        ### 👥 테스트 계정
                        - **관리자**: `admin` / `admin123`
                        - **일반사용자**: `user` / `user123`
                        
                        ### 🗄️ 데이터베이스
                        - **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
                        - **JDBC URL**: `jdbc:h2:mem:bookman`
                        - **Username**: `sa`
                        - **Password**: (비어있음)
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("BookMan Development Team")
                        .email("dev@bookman.com")
                        .url("https://github.com/bookman"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));

        // JWT 보안 스키마
        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
