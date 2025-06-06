# 📝 BookMan 프로젝트 커밋 컨벤션



## 📋 기본 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 예시
```
feat(book): 도서 검색 기능 추가

사용자가 제목, 저자, ISBN으로 도서를 검색할 수 있도록 구현
- 페이징 처리
- 정렬 옵션 추가

Closes #123
```

## 🏷️ Type (필수)

| Type    | 설명 | 예시 |
|---------|------|------|
| `feat`  | 새로운 기능 추가 | `feat(auth): 소셜 로그인 기능 구현` |
| `fix`   | 버그 수정 | `fix(book): 도서 삭제 시 NPE 오류 해결` |
| `docs`  | 문서 변경 | `docs(api): Swagger 문서 업데이트` |
| `style` | 코드 포매팅, 세미콜론 누락 등 | `style(user): 코드 포매팅 적용` |
| `refactor` | 코드 리팩터링 | `refactor(service): BookService 메소드 분리` |
| `perf`  | 성능 개선 | `perf(query): 도서 검색 쿼리 최적화` |
| `test`  | 테스트 코드 추가/수정 | `test(book): BookService 단위 테스트 추가` |
| `chore` | 빌드, 설정 파일 등 | `chore(deps): Spring Boot 3.2로 업그레이드` |
| `ci`    | CI/CD 관련 | `ci(github): 자동 배포 워크플로우 추가` |
| `build` | 빌드 시스템 변경 | `build(gradle): 의존성 버전 업데이트` |
| `revert` | 이전 커밋 되돌리기 | `revert: "feat(book): 도서 검색 기능 추가"` |

## 🎯 Scope (선택사항)

BookMan 프로젝트의 주요 스코프:

| Scope       | 설명            |
|-------------|---------------|
| `{package}` | '{pakage}' 기능 |
| `db`        | 데이터베이스 관련     |
| `config`    | 설정 관련         |
| `security`  | 보안 관련         |

## ✍️ Subject (필수)

- **50자 이내**로 작성
- **한글 또는 영어** 사용 (팀 내 합의된 언어)
- **명령문** 형태로 작성 ("추가했음" ❌, "추가" ⭕)
- **첫 글자는 소문자** (영어의 경우)
- **마침표 사용 금지**

### 좋은 예시
```
feat(book): 도서 대출 기능 추가
fix(auth): 로그인 토큰 만료 처리 오류 수정
docs(readme): 프로젝트 설치 가이드 추가
```

### 나쁜 예시
```
feat: 기능 추가했음.
fix(book): Bug fix
update: 코드 수정
```

## 📝 Body (선택사항)

- **72자마다 줄바꿈**
- **무엇을, 왜** 변경했는지 설명
- **어떻게**는 코드로 설명되므로 생략

```
feat(book): 도서 대출 연장 기능 구현

기존 7일 대출 기간을 사용자 요청에 따라 최대 3회까지 
연장할 수 있도록 기능을 추가함

- 연장 횟수 제한 로직 구현
- 연체 도서는 연장 불가 처리
- 예약 대기자가 있는 경우 연장 제한
```

## 🔗 Footer (선택사항)

### Breaking Changes
```
BREAKING CHANGE: User API의 응답 형식이 변경됨

기존 { user: {...} } 형태에서 
{ data: { user: {...} } } 형태로 변경
```

### 이슈 참조
```
Closes #123
Fixes #456, #789
Resolves #999
```

## 🚀 실제 사용 예시

### 기능 추가
```
feat(book): 도서 리뷰 작성 기능 추가

사용자가 대출한 도서에 대해 리뷰를 작성할 수 있도록 구현
- 별점 1-5점 평가
- 텍스트 리뷰 작성
- 리뷰 수정/삭제 기능

Closes #234
```

### 버그 수정
```
fix(search): 특수문자 검색 시 오류 해결

검색어에 특수문자가 포함된 경우 SQL 예외가 발생하는 
문제를 해결함

- 특수문자 이스케이프 처리 추가
- 검색어 유효성 검증 로직 강화

Fixes #156
```

### 리팩터링
```
refactor(service): BookService 코드 구조 개선

복잡한 메소드를 작은 단위로 분리하여 가독성과 
테스트 용이성을 향상시킴

- searchBooks 메소드를 여러 private 메소드로 분리
- 책임에 따른 클래스 분리
- 불필요한 의존성 제거
```

### 테스트 추가
```
test(author): AuthorService 통합 테스트 추가

AuthorService의 CRUD 기능에 대한 통합 테스트를 작성하여
코드 커버리지를 85%로 향상시킴

- 저자 생성/수정/삭제 테스트
- 예외 상황 테스트 케이스 추가
- TestContainer를 활용한 DB 테스트
```

## 🛠️ Git Hooks 설정 (선택사항)

커밋 메시지 형식을 자동으로 검증하려면 다음 스크립트를 `.git/hooks/commit-msg`에 추가:

```bash
#!/bin/sh
commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build|revert)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "Invalid commit message format!"
    echo "Format: <type>(<scope>): <subject>"
    echo "Example: feat(book): 도서 검색 기능 추가"
    exit 1
fi
```

## 📚 참고 자료

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Angular Commit Message Guidelines](https://github.com/angular/angular/blob/main/CONTRIBUTING.md#commit)
- [Semantic Versioning](https://semver.org/)

---

**💡 팁**: IDE의 커밋 템플릿 기능을 활용하면 더 쉽게 컨벤션을 따를 수 있습니다!
