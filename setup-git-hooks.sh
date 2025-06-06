#!/bin/bash

# BookMan 프로젝트 Git 설정 스크립트
# 팀원들이 쉽게 커밋 컨벤션을 적용할 수 있도록 도와주는 스크립트

echo "🔧 BookMan 프로젝트 Git 설정을 시작합니다..."

# 1. Git Hook 경로 설정
echo "📁 Git Hook 경로 설정 중..."
git config core.hooksPath .githooks
if [ $? -eq 0 ]; then
    echo "✅ Git Hook 경로가 .githooks로 설정되었습니다."
else
    echo "❌ Git Hook 경로 설정에 실패했습니다."
    exit 1
fi

# 2. 커밋 템플릿 설정
echo "📝 커밋 메시지 템플릿 설정 중..."
git config commit.template .gitmessage
if [ $? -eq 0 ]; then
    echo "✅ 커밋 템플릿이 설정되었습니다."
else
    echo "❌ 커밋 템플릿 설정에 실패했습니다."
    exit 1
fi

# 3. Hook 파일 실행 권한 부여
echo "🔐 Hook 파일 실행 권한 설정 중..."
chmod +x .githooks/commit-msg
if [ $? -eq 0 ]; then
    echo "✅ commit-msg Hook 실행 권한이 설정되었습니다."
else
    echo "❌ Hook 실행 권한 설정에 실패했습니다."
    exit 1
fi

# 4. Git 사용자 정보 확인
echo "👤 Git 사용자 정보 확인 중..."
GIT_USER_NAME=$(git config user.name)
GIT_USER_EMAIL=$(git config user.email)

if [ -z "$GIT_USER_NAME" ] || [ -z "$GIT_USER_EMAIL" ]; then
    echo "⚠️  Git 사용자 정보가 설정되지 않았습니다."
    echo "📝 다음 명령어로 설정해주세요:"
    echo "   git config user.name \"Your Name\""
    echo "   git config user.email \"your.email@example.com\""
    echo ""
else
    echo "✅ Git 사용자 정보:"
    echo "   이름: $GIT_USER_NAME"
    echo "   이메일: $GIT_USER_EMAIL"
fi

# 5. 설정 완료 메시지
echo ""
echo "🎉 Git 설정이 완료되었습니다!"
echo ""
echo "📋 이제 다음과 같이 사용할 수 있습니다:"
echo ""
echo "1️⃣  커밋할 때 템플릿 사용:"
echo "   git commit"
echo "   (에디터에 커밋 템플릿이 자동으로 표시됩니다)"
echo ""
echo "2️⃣  커밋 메시지 형식이 자동으로 검증됩니다:"
echo "   올바른 형식: feat(book): 도서 검색 기능 추가"
echo "   잘못된 형식: 기능 추가 (자동으로 거부됩니다)"
echo ""
echo "3️⃣  자세한 가이드는 다음 파일을 참고하세요:"
echo "   📖 COMMIT_CONVENTION.md"
echo "   📖 README.md"
echo ""
echo "💡 팁: IDE의 Git 통합 기능을 사용해도 Hook이 작동합니다!"
