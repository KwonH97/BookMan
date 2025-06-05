# 📚 BookMan - 도서 관리 시스템

## 🎯 프로젝트 개요

BookMan은 TDD(Test-Driven Development) 방식으로 개발된 Spring Boot 기반의 도서 관리 시스템입니다.
JWT 토큰 기반 인증과 권한 관리, RESTful API를 제공합니다.

## 🏗️ 기술 스택

### Backend
- **Java 21** - 최신 LTS 버전 + **Virtual Thread 지원**
- **Spring Boot 3.5.0** - 웹 애플리케이션 프레임워크
- **Spring Security 6** - 인증 및 권한 관리
- **Spring Data JPA** - 데이터 액세스 계층
- **JWT (JSON Web Token)** - 토큰 기반 인증
- **H2 Database** - 임베드 데이터베이스 (개발용)
- **MySQL** - 프로덕션 데이터베이스 (추후 적용)
- **Virtual Threads** - JDK 21의 경량 스레드로 높은 동시성 처리

### Development & Testing
- **Gradle 8.14** - 빌드 도구
- **JUnit 5** - 테스트 프레임워크
- **AssertJ** - 테스트 어설션 라이브러리
- **Mockito** - 모킹 프레임워크
- **TDD** - 테스트 주도 개발 방법론

### Documentation
- **Swagger/OpenAPI 3** - API 문서화
- **Spring Boot DevTools** - 개발 편의성

## 🚀 빠른 시작

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd BookMan
```

### 2. 애플리케이션 실행
```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 BookManApplication.java 실행
```

### 3. 접속 확인
- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bookman`
  - Username: `sa`
  - Password: (비어있음)
- **Virtual Thread 성능 테스트**: http://localhost:8080/swagger-ui.html#/Virtual%20Thread%20Performance

## 👥 테스트 계정

| 역할 | 사용자명 | 비밀번호 | 설명 |
|------|----------|----------|------|
| 관리자 | `admin` | `admin123` | 모든 권한 보유 |
| 일반사용자 | `user` | `user123` | 기본 사용자 권한 |

## 🧪 TDD 개발 방식

### 테스트 전략
```
📁 src/test/java/
├── 🧪 Unit Tests        # 단위 테스트
│   ├── service/         # 비즈니스 로직 테스트
│   ├── util/           # 유틸리티 클래스 테스트
│   └── repository/     # 데이터 액세스 테스트
├── 🔗 Integration Tests # 통합 테스트
│   ├── controller/     # API 엔드포인트 테스트
│   └── security/       # 보안 설정 테스트
└── 🏗️ Test Infrastructure
    ├── BaseIntegrationTest.java
    ├── TestDataFactory.java
    └── TestRunner.java
```

### TDD 사이클
1. **🔴 Red**: 실패하는 테스트 작성
2. **🟢 Green**: 테스트를 통과하는 최소한의 코드 작성
3. **🔵 Refactor**: 코드 개선 및 리팩토링

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "AuthControllerTest"

# 테스트 리포트 확인
# build/reports/tests/test/index.html
```

## 📖 API 가이드

### 🔐 인증 API

#### 회원가입
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "fullName": "새로운 사용자"
}
```

#### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 토큰 검증
```http
GET /api/auth/validate
Authorization: Bearer {jwt-token}
```

### 👤 사용자 API

#### 내 정보 조회
```http
GET /api/users/me
Authorization: Bearer {jwt-token}
```

#### 관리자 전용 API
```http
GET /api/users/admin-only
Authorization: Bearer {admin-jwt-token}
```

### ⚡ Virtual Thread 성능 API

#### 스레드 정보 조회
```http
GET /api/performance/thread-info
```

#### Virtual Thread 스트레스 테스트
```http
POST /api/performance/stress-test?taskCount=1000&delayMs=100
```

## 🏛️ 아키텍처

### 계층 구조
```
📁 src/main/java/com/bookman/
├── 🚀 BookManApplication.java          # 메인 애플리케이션
├── 🔐 auth/                            # 인증 도메인
│   ├── config/                         # 설정 (JWT, Security)
│   ├── controller/                     # REST 컨트롤러
│   ├── dto/                           # 데이터 전송 객체
│   ├── filter/                        # 보안 필터
│   ├── service/                       # 비즈니스 로직
│   └── util/                          # 유틸리티
├── 👤 user/                           # 사용자 도메인
│   ├── controller/                    # 사용자 API
│   ├── dto/                          # 응답 객체
│   ├── entity/                       # JPA 엔티티
│   └── repository/                   # 데이터 액세스
├── 📚 book/                          # 도서 도메인 (향후 확장)
├── ✍️ author/                        # 작가 도메인 (향후 확장)
├── ⚙️ config/                        # 전역 설정
└── 🚨 common/                        # 공통 기능
    └── exception/                    # 예외 처리
```

### 보안 설정
- **JWT 토큰**: 24시간 유효
- **비밀번호**: BCrypt 암호화
- **CORS**: 개발환경용 설정
- **세션**: Stateless (JWT 사용)

## 🔧 개발 환경 설정

### IDE 설정 (IntelliJ IDEA 권장)
1. Project SDK: Java 21
2. Gradle JVM: Java 21
3. Annotation Processing 활성화 (Lombok)
4. Code Style: Google Java Style

### 환경 변수 (application.properties)
```properties
# JWT 설정
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# 데이터베이스 설정 (MySQL 사용시)
spring.datasource.url=jdbc:mysql://localhost:3306/bookman
spring.datasource.username=your-username
spring.datasource.password=your-password
```

## 🧪 테스트 커버리지

현재 테스트 커버리지:
- **Controller Layer**: 100%
- **Service Layer**: 95%
- **Repository Layer**: 90%
- **Utility Classes**: 100%

## 🚧 향후 개발 계획

### Phase 1: 기본 기능 (완료)
- ✅ JWT 인증 시스템
- ✅ 사용자 관리
- ✅ API 문서화
- ✅ 테스트 인프라

### Phase 2: 도서 관리 (예정)
- 📚 도서 CRUD
- 🔍 도서 검색
- 📊 카테고리 관리
- 📈 통계 기능

### Phase 3: 고급 기능 (예정)
- 📱 모바일 앱 연동
- 🔔 알림 시스템
- 📊 대시보드
- 🔄 데이터 백업

## 🤝 기여 가이드

### 개발 워크플로우
1. **이슈 생성**: GitHub Issues에 작업 내용 등록
2. **브랜치 생성**: `feature/기능명` 또는 `bugfix/버그명`
3. **TDD 적용**: 테스트 먼저 작성 후 기능 구현
4. **코드 리뷰**: Pull Request를 통한 코드 검토
5. **병합**: 테스트 통과 후 main 브랜치 병합

### 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 스타일 변경
refactor: 코드 리팩토링
test: 테스트 코드 추가/수정
chore: 빌드 설정 등 기타 변경
```

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 문의

- **Email**: dev@bookman.com
- **GitHub**: [BookMan Repository](https://github.com/bookman)
- **Documentation**: [Wiki](https://github.com/bookman/wiki)

---

**Happy Coding! 📚✨**
