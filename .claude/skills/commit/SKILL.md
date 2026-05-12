---
description: staged 변경사항을 분석해 팀 prefix(feat/fix/refactor/test/doc/release/edit) + 한글 설명으로 커밋을 생성. 사용자가 명시적으로 "/commit" 또는 "커밋해줘"라고 호출할 때만 사용 (자동 트리거 금지).
disable-model-invocation: true
---

## 컨텍스트

staged 변경 요약:
!`git diff --cached --stat`

staged 변경 본문 (앞부분):
!`git diff --cached | head -200`

unstaged 변경 (참고용, 커밋에 포함 X):
!`git diff --stat`

최근 커밋 스타일:
!`git log --pretty=format:'%s' -10`

## 지침

### 1. staged 비어 있으면 종료
`git diff --cached --stat` 출력이 비어 있으면 사용자에게 "staged 변경이 없다. 먼저 `git add <files>` 해라."라고 알리고 종료. **unstaged를 임의로 `git add` 하지 말 것.**

### 2. prefix 추론
staged diff 내용을 보고 아래 중 하나 선택:
- `feat`: 새 기능 / 엔드포인트 / 도메인 메서드 추가
- `fix`: 버그 수정
- `refactor`: 동작 변경 없이 구조 / 네이밍 / 패키지 개선
- `test`: 테스트 코드만 추가·수정
- `doc`: README / JavaDoc / 주석만 변경
- `release`: 버전 / 릴리즈 관련
- `edit`: 위에 안 맞는 자잘한 편집 (사소한 텍스트 수정 등)

테스트 파일과 프로덕션 코드가 함께 변경됐다면 의도가 큰 쪽의 prefix 사용 (보통 `feat` 또는 `fix`).

### 3. 메시지 작성
형식: `{prefix}: {한글 한 줄 설명}`
- 무엇을(what), 필요하면 왜(why)를 한 줄에. 변경 파일을 나열하지 말고 **의도**를 적는다.
- 좋은 예: `refactor: Gender 관련 byte를 enum으로 변경`
- 나쁜 예: `fix: ElderService.java, MemberService.java 수정`

### 4. 사용자 확인
제안 메시지를 보여주고 OK / 수정 요청을 받는다. 수정 요청이 있으면 반영해 다시 제안. **확인 없이 커밋 금지.**

### 5. 커밋 실행
HEREDOC으로 메시지 전달:

```bash
git commit -m "$(cat <<'EOF'
{prefix}: {한글 설명}
EOF
)"
```

### 6. 금지 사항
- `--no-verify` 로 hook 건너뛰지 않는다. pre-commit hook 실패 시 원인 수정 후 **새 커밋** (--amend 아님).
- `git add -A` / `git add .` 금지. 사용자가 stage한 것만 커밋.
- `--amend` 는 사용자가 명시 요청할 때만.
- Co-Authored-By 트레일러 자동 추가 금지 (팀 컨벤션에 없음).

### 7. 커밋 후 보고
`git status`로 결과 확인하고 한 줄 보고. "커밋 abc1234 생성. 남은 unstaged: N개." 형식.
