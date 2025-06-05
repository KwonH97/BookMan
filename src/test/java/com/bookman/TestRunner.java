package com.bookman;

import org.junit.platform.suite.api.*;

/**
 * BookMan 전체 테스트 스위트
 * 
 * 이 클래스는 프로젝트의 모든 테스트를 조직화하여 실행하는 JUnit 5 테스트 스위트입니다.
 * 
 * 실행 방법:
 * 1. IDE에서: 이 클래스를 우클릭하고 "Run TestRunner" 선택
 * 2. Gradle에서: ./gradlew test --tests "com.bookman.TestRunner"
 * 
 * 포함된 테스트 패키지:
 * - com.bookman.auth: 인증 관련 테스트 (컨트롤러, 서비스, 유틸)
 * - com.bookman.user: 사용자 관리 테스트 (리포지토리, 서비스)
 * - com.bookman.common: 공통 테스트 (통합 테스트 기반 클래스)
 * - com.bookman.performance: 성능 테스트 (Virtual Thread 등)
 */
@Suite
@SuiteDisplayName("📚 BookMan 전체 테스트 스위트")
@SelectPackages({
    "com.bookman.auth",
    "com.bookman.user", 
    "com.bookman.common",
    "com.bookman.performance"
})
@IncludeClassNamePatterns({
    ".*Test",           // *Test.java 클래스들
    ".*Tests"           // *Tests.java 클래스들  
})
@ExcludeClassNamePatterns({
    ".*IntegrationTest", // 통합 테스트는 별도 실행
    ".*BaseTest.*",      // Base 테스트 클래스들 제외
    "TestRunner"         // 자기 자신 제외
})
public class TestRunner {
    
    /**
     * 이 클래스는 JUnit 5 테스트 스위트를 정의합니다.
     * 
     * 주요 기능:
     * - 모든 단위 테스트를 한 번에 실행
     * - 안정적인 순차 실행으로 안정성 보장
     * - 패키지별 테스트 조직화
     * - 통합 테스트는 별도로 분리
     * 
     * 실행 결과:
     * - 각 패키지별 테스트 결과 표시
     * - 전체 테스트 통계 제공
     * - 실패한 테스트에 대한 상세 정보
     */
}
