#!/bin/bash

# BookMan API 테스트 스크립트
# 사용법: ./test-api.sh

BASE_URL="http://localhost:8080"
ADMIN_TOKEN=""
USER_TOKEN=""

echo "🚀 BookMan API 테스트 시작"
echo "================================"

# 1. 관리자 로그인
echo "1️⃣ 관리자 로그인 테스트..."
ADMIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$ADMIN_TOKEN" ]; then
    echo "✅ 관리자 로그인 성공"
    echo "🔑 Admin Token: ${ADMIN_TOKEN:0:20}..."
else
    echo "❌ 관리자 로그인 실패"
    exit 1
fi

# 2. 일반 사용자 로그인
echo ""
echo "2️⃣ 일반 사용자 로그인 테스트..."
USER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }')

USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$USER_TOKEN" ]; then
    echo "✅ 일반 사용자 로그인 성공"
    echo "🔑 User Token: ${USER_TOKEN:0:20}..."
else
    echo "❌ 일반 사용자 로그인 실패"
    exit 1
fi

# 3. 새 사용자 회원가입
echo ""
echo "3️⃣ 새 사용자 회원가입 테스트..."
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$(date +%s)'",
    "email": "testuser_'$(date +%s)'@test.com",
    "password": "password123",
    "fullName": "테스트 사용자"
  }')

if echo $REGISTER_RESPONSE | grep -q "token"; then
    echo "✅ 회원가입 성공"
else
    echo "❌ 회원가입 실패"
fi

# 4. 관리자 권한 테스트
echo ""
echo "4️⃣ 관리자 권한 테스트..."
ADMIN_TEST=$(curl -s -X GET "${BASE_URL}/api/users/admin-only" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

if echo $ADMIN_TEST | grep -q "관리자만"; then
    echo "✅ 관리자 권한 접근 성공"
else
    echo "❌ 관리자 권한 접근 실패"
fi

# 5. 일반 사용자 권한 제한 테스트
echo ""
echo "5️⃣ 일반 사용자 권한 제한 테스트..."
USER_ADMIN_TEST=$(curl -s -w "%{http_code}" -X GET "${BASE_URL}/api/users/admin-only" \
  -H "Authorization: Bearer $USER_TOKEN")

if echo $USER_ADMIN_TEST | grep -q "403"; then
    echo "✅ 일반 사용자 관리자 API 접근 차단 성공"
else
    echo "❌ 권한 제어 실패"
fi

# 6. 내 정보 조회 테스트
echo ""
echo "6️⃣ 내 정보 조회 테스트..."
MY_INFO=$(curl -s -X GET "${BASE_URL}/api/users/me" \
  -H "Authorization: Bearer $USER_TOKEN")

if echo $MY_INFO | grep -q "username"; then
    echo "✅ 내 정보 조회 성공"
else
    echo "❌ 내 정보 조회 실패"
fi

# 7. Virtual Thread 정보 확인
echo ""
echo "7️⃣ Virtual Thread 정보 확인..."
THREAD_INFO=$(curl -s -X GET "${BASE_URL}/api/performance/thread-info")

if echo $THREAD_INFO | grep -q "isVirtual"; then
    echo "✅ Virtual Thread 정보 조회 성공"
    echo "🧵 $(echo $THREAD_INFO | grep -o '"threadInfo":"[^"]*' | cut -d'"' -f4)"
    echo "⚡ Virtual: $(echo $THREAD_INFO | grep -o '"isVirtual":[^,]*' | cut -d':' -f2)"
else
    echo "❌ Virtual Thread 정보 조회 실패"
fi

# 8. Virtual Thread 스트레스 테스트
echo ""
echo "8️⃣ Virtual Thread 스트레스 테스트..."
STRESS_TEST=$(curl -s -X POST "${BASE_URL}/api/performance/stress-test?taskCount=100&delayMs=50")

if echo $STRESS_TEST | grep -q "taskCount"; then
    echo "✅ Virtual Thread 스트레스 테스트 성공"
    TOTAL_TIME=$(echo $STRESS_TEST | grep -o '"totalTimeMs":[^,]*' | cut -d':' -f2)
    SUCCESS_COUNT=$(echo $STRESS_TEST | grep -o '"successCount":[^,]*' | cut -d':' -f2)
    echo "⏱️ 총 처리 시간: ${TOTAL_TIME}ms"
    echo "✅ 성공한 작업: ${SUCCESS_COUNT}개"
else
    echo "❌ Virtual Thread 스트레스 테스트 실패"
fi

# 9. 토큰 검증 테스트
echo ""
echo "9️⃣ 토큰 검증 테스트..."
TOKEN_VALIDATION=$(curl -s -X GET "${BASE_URL}/api/auth/validate" \
  -H "Authorization: Bearer $USER_TOKEN")

if echo $TOKEN_VALIDATION | grep -q "토큰이 유효"; then
    echo "✅ 토큰 검증 성공"
else
    echo "❌ 토큰 검증 실패"
fi

echo ""
echo "================================"
echo "🎉 API 테스트 완료!"
echo ""
echo "📊 추가 확인사항:"
echo "- Swagger UI: ${BASE_URL}/swagger-ui.html"
echo "- H2 Console: ${BASE_URL}/h2-console"
echo "- API Docs: ${BASE_URL}/api-docs"
echo "- Virtual Thread 성능: ${BASE_URL}/swagger-ui.html#/Virtual%20Thread%20Performance"
echo "- Actuator Health: ${BASE_URL}/actuator/health"
echo "- Thread Dump: ${BASE_URL}/actuator/threaddump"
