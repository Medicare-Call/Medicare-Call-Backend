---
description: 리팩토링/수정이 "건드리면 안 되는 영역"(Flyway 기존 파일, ErrorCode, application.yml, JWT/Security)에 손댔는지 빠르게 점검. 사용자가 "리팩 안전한가", "위험한 영역 건드렸나", "머지해도 되나", "/refactor-check"라고 할 때 사용.
---

## 컨텍스트 (변경 파일 목록과 위험 영역 diff만 — 가볍게)

main 기준 변경된 파일 목록:
!`git diff main...HEAD --name-only`

수정된 Flyway 마이그레이션 파일 (존재하면 거의 확실히 blocker):
!`git diff main...HEAD --name-status -- 'src/main/resources/db/migration/V*.sql'`

ErrorCode / GlobalExceptionHandler 변경:
!`git diff main...HEAD -- 'src/main/java/com/example/medicare_call/global/enums/ErrorCode.java' 'src/main/java/com/example/medicare_call/global/GlobalExceptionHandler.java' 'src/main/java/com/example/medicare_call/global/exception/' | head -120`

환경설정 변경 (application.yml / docker-compose / workflows):
!`git diff main...HEAD --name-only -- 'src/main/resources/application.yml' 'src/main/resources/application.properties' 'docker-compose.yml' '.github/workflows/'`
!`git diff main...HEAD -- 'src/main/resources/application.yml' 'docker-compose.yml' | grep -E '^[+-].*\$\{' | head -40`

JWT / Security 변경:
!`git diff main...HEAD --name-only -- 'src/main/java/com/example/medicare_call/global/jwt/' '**/SecurityConfig.java' '**/RequestLoggingFilter.java'`

## 지침

**위 컨텍스트만 보고** 결과를 보고하라. 코드 추가 탐색 금지 — 이 skill은 빠른 게이트지 풀 리뷰가 아니다.

다음 형식으로 출력:

### 🚫 Blocker
- 적용된 `V*.sql` 파일이 modify(M) 상태로 잡힘 → "새 `V{n+1}__{snake_case}.sql`로 분리해라. Flyway는 적용된 파일을 재실행하지 않는다."
- `ErrorCode` enum 값 삭제/리네임 라인 발견 → "클라이언트 계약이 깨진다. 클라이언트 팀과 사전 합의 후 진행해라."

### ⚠️ Sync required
- `application.yml`에 `${ENV_VAR}` 라인 +/- → "동기화 체크: ① `docker-compose.yml` ② GitHub Actions Secrets ③ EC2 환경. 셋 다 갱신 전 머지 금지."
- `global/jwt/` 또는 `SecurityConfig` 변경 → "인증 흐름 회귀 위험. 로그인/토큰 갱신 통합 테스트로 검증 권장."

### ✅ OK
- 위 영역 변경 없음 → "위험 영역 미접촉. 일반 리뷰는 `/review`로."

**규칙**:
- 각 항목 한 줄로 끝낼 것. 장황 금지.
- 위 컨텍스트가 비어 있는 섹션은 조용히 넘긴다. "변경 없음"을 일일이 출력하지 않는다.
- 직접 파일 리뷰 X, 일반 코드 품질 지적 X — 위험 신호만 raise.
