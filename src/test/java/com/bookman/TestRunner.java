package com.bookman;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("BookMan 전체 테스트 스위트")
@SelectPackages({
    "com.bookman.auth",
    "com.bookman.user",
    "com.bookman.common"
})
public class TestRunner {
    // 이 클래스는 JUnit 5 테스트 스위트를 정의합니다.
    // 모든 테스트를 한 번에 실행할 때 사용합니다.
}
