---
description: 현재 브랜치 변경사항을 팀 컨벤션(CLAUDE.md) 기준으로 자체 리뷰. 사용자가 "리뷰해줘", "내 변경 점검해줘", "PR 올리기 전에 봐줘", "/review"라고 할 때 사용.
---

## 컨텍스트

현재 브랜치: !`git rev-parse --abbrev-ref HEAD`

main 기준 변경 파일:
!`git diff main...HEAD --stat`

브랜치 커밋 목록:
!`git log main..HEAD --oneline`

unstaged + staged diff (참고용 요약):
!`git diff HEAD --stat`

## 지침

위 변경사항을 **직접 리뷰하지 말고**, `code-reviewer` subagent를 호출해 독립적 의견을 받는다.

### Agent 호출 방법
- `subagent_type`: `code-reviewer`
- `description`: "Branch review against CLAUDE.md"
- `prompt`: 다음을 포함해 전달
  1. 위 컨텍스트의 변경 파일 목록 (전체 복사)
  2. "리포 루트 CLAUDE.md의 컨벤션과 `.claude/agents/code-reviewer.md` 체크리스트 a~f를 적용해 리뷰해라"
  3. "각 지적사항을 **blocker / warning / nit** 중 하나로 분류해서 보고해라"

### subagent 결과 처리
1. subagent가 반환한 결과를 그대로 신뢰하지 말고, blocker로 분류된 항목은 해당 파일을 한 번 직접 확인해 사실 여부 검증.
2. 사용자에게 다음 순서로 보고:
   - **🚫 Blocker** (머지 전 반드시 수정)
   - **⚠️ Warning** (수정 권장)
   - **💭 Nit** (선택)
3. 사용자가 수정에 동의하면 그때만 직접 Edit으로 적용. 동의 없이 코드 변경 금지.

### 호출 시점
사용자가 "리뷰해줘" 류의 발화를 하면 즉시 이 skill을 invoke. 변경이 비어 있으면 "main과 차이 없음"이라 보고하고 종료.
