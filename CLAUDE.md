# Medicare-Call-Backend

Spring Boot 3.2.4 / Java 17 / Gradle. MySQL + Flyway + Redis 기반 AI 케어콜 백엔드.

## 빌드 & 테스트
- `./gradlew clean build` — CI가 모든 push/PR에서 실행 (`.github/workflows/ci-workflow.yml`)
- `./gradlew test` — JUnit 5. 테스트 `@DisplayName`은 한글로 작성하는 컨벤션
- 단일 테스트: `./gradlew test --tests "ClassName.methodName"`

## 디렉토리
`src/main/java/com/example/medicare_call` 아래 계층형: `controller / service / repository / dto / mapper / domain / scheduler / global / api / util`.
- **WHY 계층형(DDD 아님)**: 도메인 20개지만 팀 규모와 온보딩 비용을 고려해 모듈 분리 대신 패키지로 정리. 도메인 모듈화 PR은 사전 논의 필요.
- 변환은 반드시 `mapper/` 또는 Service 내부에서. Entity가 Controller까지 노출되면 리뷰에서 막힘.

## 핵심 컨벤션
- **DTO**: Lombok `@Data @Builder` class (record 미사용). 응답에 Entity 직접 반환 금지.
- **예외**: `CustomException(ErrorCode)`만 사용. 일반 `RuntimeException` 직접 throw 금지, generic `Exception` catch 금지. 매핑은 `global/GlobalExceptionHandler`가 단일 진입점.
- **트랜잭션**: Service 메서드 레벨에 `@Transactional`. 조회 전용은 `@Transactional(readOnly = true)`로 명시 분리. Controller/Repository에는 붙이지 않음.
- **테스트**: Controller=`@WebMvcTest`, Repository=`@DataJpaTest`, 비즈니스 로직=POJO 단위 테스트. 신규 비즈니스 메서드는 테스트 누락 시 머지 불가.

## 건드릴 때 주의
- **Flyway `src/main/resources/db/migration/V*.sql`**: 이미 적용된 파일은 절대 수정 금지. 항상 새 `V{n+1}__{snake_case}.sql` 추가 — Flyway는 적용된 파일을 재실행하지 않으므로 기존 파일 수정은 운영 DB 정의와 어긋남.
- **`application.yml` / 환경변수**: `${ENV_VAR}` 키 추가·리네임 시 `docker-compose.yml`, GitHub Actions Secrets, EC2 환경 셋을 모두 동기화. 셋 다 갱신 전에는 머지 금지.

## 진행 중 마이그레이션
- **byte/tinyint → String ENUM** (V33–V35 진행 중). 신규 코드에서 enum 컬럼은 String으로 매핑. 기존 `byte`/`Byte` 필드를 건드리기 전에 진행 중인 enum 변환 PR과 충돌 여부 먼저 확인.

## 커밋 & PR
- **브랜치명**: `{prefix}/{kebab-case-english}` 형식. 예: `refactor/byte-to-enum-migration`. 추천이 필요하면 변경 내용 보여주고 자연어로 부탁 — Claude가 `git diff` 보고 제안한다 (별도 skill 없음).
- **커밋/PR 제목**: `{prefix}: {한글 설명}` 형식. 예: `refactor: Gender 관련 byte를 enum으로 변경`. prefix는 둘 다 동일: `feat | fix | refactor | test | doc | release | edit`.
- `--no-verify`로 hook을 건너뛰지 않는다.
- PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md` 형식을 따른다 (요약+배경 / 테스트 계획 / DB 영향 / 체크리스트).
- 브랜치 prefix로 라벨이 자동 부여됨 (`.github/labeler.yml`).
