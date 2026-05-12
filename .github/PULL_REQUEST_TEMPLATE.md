<!-- 브랜치/PR 제목은 `{prefix}: {한글 설명}` 형식 (prefix: feat | fix | refactor | test | doc | release | edit) -->

## 변경 요약 + 배경 (왜)

<!-- 무엇을 바꿨고 왜 바꿨는지. 관련 이슈/요구사항 링크 첨부. -->

- 무엇:
- 왜:
- 관련 이슈: #

## 테스트 계획

<!-- 어떤 테스트로 변경을 검증했는지. 외부 API/케어콜/결제처럼 자동화 어려운 부분은 수동 검증 단계도 적어주세요. -->

- [ ] 단위 테스트 추가/갱신 (해당 시)
- [ ] 통합 테스트 (`@WebMvcTest` / `@DataJpaTest` / `@SpringBootTest`) 추가/갱신 (해당 시)
- [ ] 수동 검증 단계:
  - 

## DB 마이그레이션 영향

<!-- Flyway 변경이 있다면 반드시 작성. 없으면 "해당 없음"으로 두기. -->

- 추가 마이그레이션 파일: `V{n}__{name}.sql` (없으면 "해당 없음")
- NOT NULL 컬럼 추가 / 데이터 백필 필요 여부:
- 운영 적용 시 주의사항 (다운타임, 순서 등):

## 체크리스트

- [ ] CI(`./gradlew clean build`) 통과
- [ ] 신규 비즈니스 메서드에 테스트 동반
- [ ] Entity가 Controller까지 노출되지 않음 (DTO 변환 확인)
- [ ] `@Transactional` 위치/`readOnly` 분리 확인
- [ ] `CustomException(ErrorCode)` 사용, generic Exception throw/catch 없음
- [ ] **환경변수 추가/리네임 시**: `application.yml` + `docker-compose.yml` + GitHub Actions Secrets + EC2 환경 모두 동기화 (해당 없으면 체크)
- [ ] **클라이언트 계약 변경 시** (응답 스키마 / `ErrorCode` / 엔드포인트): 프론트 팀과 사전 합의
- [ ] README / API 문서 업데이트 (해당 시)
