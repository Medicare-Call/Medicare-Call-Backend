---
description: 현재 브랜치를 origin에 push하고 `.github/PULL_REQUEST_TEMPLATE.md` 형식으로 PR을 생성. 커밋은 이미 끝난 상태를 가정 — uncommitted 변경이 있으면 중단하고 `/commit` 안내. base는 항상 main. 사용자가 명시적으로 "/pr", "PR 올려줘", "PR 만들어"라고 호출할 때만 사용.
disable-model-invocation: true
---

## 컨텍스트

현재 브랜치:
!`git rev-parse --abbrev-ref HEAD`

작업 트리 상태:
!`git status --short`

gh CLI 인증 확인:
!`gh auth status 2>&1 | head -5`

main 기준 커밋 목록:
!`git log main..HEAD --pretty=format:'%h %s' 2>/dev/null`

main 기준 변경 파일 요약:
!`git diff main...HEAD --stat`

Flyway 마이그레이션 변경 (PR 본문 DB 섹션 자동 채움 / 기존 파일 수정은 가드):
!`git diff main...HEAD --name-status -- 'src/main/resources/db/migration/V*.sql'`

테스트 변경 (PR 본문 테스트 섹션 자동 언급):
!`git diff main...HEAD --name-only -- 'src/test/'`

PR 템플릿:
@.github/PULL_REQUEST_TEMPLATE.md

## 지침

### 1. 중단 조건 (하나라도 해당하면 즉시 멈춤, 임의 조치 금지)

- `gh auth status` 출력이 인증 실패면 → "`gh auth login` 후 다시 시도해라" 안내 후 종료.
- `git status --short`에 출력이 있으면 (uncommitted 변경) → "uncommitted 변경이 있다. 먼저 `/commit`으로 커밋해라. 이 skill은 커밋을 대신 하지 않는다." 안내 후 종료.
- 현재 브랜치가 `main`이면 → 종료하되, 다음 정보를 함께 출력해 사용자가 곧장 이어갈 수 있게 한다:
  1. "main에서 직접 PR을 만들 수 없다."
  2. 위 diff stat을 보고 **prefix(feat/fix/refactor/test/doc/release/edit)와 kebab-case 영문 이름을 한 가지 추천**.
  3. 정리 명령 한 묶음 제시:
     ```
     git checkout -b {추천한 prefix}/{추천한 이름}
     # uncommitted 변경이 있으면 → /commit
     /pr
     ```
- `git log main..HEAD`가 비면 (main과 차이 없음) → "main과 차이가 없다. PR 만들 변경이 없다." 안내 후 종료.
- Flyway diff에 `M` status가 보이면 (기존 V*.sql 수정) → "🚫 기존 마이그레이션 파일 수정은 머지 불가다. 새 `V{n+1}__{snake_case}.sql`로 분리한 뒤 재실행해라." 안내 후 종료. (CLAUDE.md 규칙)

### 2. PR title 구성

- 커밋이 1개면 그 커밋 제목을 그대로 사용.
- 커밋이 여러 개면 의도가 가장 큰 커밋의 제목을 골라 제안. 모호하면 사용자에게 확인.
- 형식: `{prefix}: {한글 설명}` (`prefix` = feat | fix | refactor | test | doc | release | edit). PR title이 곧 자동 라벨링 키.

### 3. PR body 구성

위 컨텍스트의 PR 템플릿(`@.github/PULL_REQUEST_TEMPLATE.md`) 4섹션을 모두 채운다. 빈 칸 두지 말 것.

- **변경 요약 + 배경 (왜)**: 커밋 메시지 + diff stat을 종합해 작성. 무엇/왜/관련 이슈. 관련 이슈 번호를 모르면 한 번만 사용자에게 확인.
- **테스트 계획**:
  - `src/test/` diff에서 추가·변경된 테스트 클래스를 자동 추출해 단위/통합 체크박스에 표시.
  - 변경에 외부 호출(케어콜·결제·SMS·AI) 영역이 포함되면 "수동 검증 단계" placeholder를 사용자에게 보여주고 채워달라고 요청.
- **DB 마이그레이션 영향**:
  - 신규 `V*.sql`(`A` status)이 있으면 파일명 나열. SQL 본문에서 `NOT NULL` 컬럼 추가 / 데이터 백필 여부 추출해 기재.
  - 없으면 "해당 없음".
- **체크리스트**: 템플릿 항목 그대로 (빈 박스). 사용자가 PR 페이지에서 직접 채우도록 둔다.

### 4. 사용자 확인

작성한 title + body 를 사용자에게 보여주고 OK / 수정 요청을 받는다. **확인 없이 PR 생성 금지.**

### 5. push + PR 생성

```bash
git push -u origin "$(git rev-parse --abbrev-ref HEAD)"

gh pr create \
  --base main \
  --title "{prefix}: {한글 설명}" \
  --body "$(cat <<'EOF'
{4섹션을 채운 본문}
EOF
)"
```

push 시 non-fast-forward 충돌이 나면 즉시 중단하고 사용자에게 알린다.

### 6. 금지

- `git add` / `git commit` 호출 금지 — 이 skill은 커밋 책임 없음.
- `git push --force` / `--force-with-lease` 금지 (사용자가 명시 요청 시에만).
- `--no-verify`로 pre-push hook 건너뛰기 금지.
- PR title/body에 `Co-Authored-By` / `🤖 Generated with Claude Code` 류 푸터 절대 추가 금지.
- `--base`는 항상 `main`. dev 등 다른 브랜치 PR이 필요하면 사용자가 직접 `gh pr create` 호출.

### 7. 결과 보고

생성된 PR URL을 출력하고, "브랜치 prefix(`{prefix}`) 기반 자동 라벨이 `.github/labeler.yml`에 의해 부여될 예정" 한 줄 안내. 끝.
