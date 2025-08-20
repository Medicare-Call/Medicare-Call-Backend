# Medicare Call Backend

매일 챙기기 어려운 부모님의 건강, AI가 전화로 대신 확인해주는 돌봄 서비스, **메디케어콜**

<p align="center">
  <img width="1920" height="1080" alt="KakaoTalk_Photo_2025-08-20-18-41-35" src="https://github.com/user-attachments/assets/ea5c399c-ba0d-4557-b20a-49911d913c4d" />
</p>

## Major Features of MediCare Call

- **간편 로그인**: 복잡한 과정 없이 문자 인증을 이용하여 간편하게 로그인하여 서비스를 이용할 수 있습니다.
- **AI 케어콜**: 설정된 시간에 AI가 자동으로 어르신께 전화를 걸어 안부를 묻고, 자연스러운 대화를 통해 건강 상태를 확인합니다. 
- **건강 데이터 자동 추출 및 분석**: 통화 내용에서 식사, 복약, 수면, 혈당 등 주요 건강 데이터를 자동으로 추출하고 AI가 분석하여 이상 징후를 감지합니다.
- **건강 리포트**: 데이터를 시각화된 리포트로 제공하여, 어르신의 건강 변화를 쉽고 빠르게 파악할 수 있도록 돕습니다.
- **간편 결제**: 네이버페이를 연동하여 복잡한 과정 없이 손쉽게 서비스 구독 및 결제가 가능합니다.

## Dependencies

### Backend
- **Framework**: Spring Boot 3.2.4
- **Language**: Java 17
- **Database**: MySQL, Flyway, Spring Data JPA
- **Security**: Spring Security, JWT
- **API Documentation**: Springdoc OpenAPI (Swagger UI)

### 주요 라이브러리
- `jjwt`: JWT 토큰 생성 및 검증
- `flyway-mysql`: DB 마이그레이션 관리
- `coolsms-sdk`: SMS 발송, 인증
- `springdoc-openapi-starter-webmvc-ui`: API 문서 자동화
- `Thymeleaf`: 서버 사이드 렌더링 (결제 WebView 페이지)
- `Naver Pay SDK`: 네이버페이 결제 연동
- `OpenAI API`: AI 기반 건강 데이터 분석 및 요약, 케어콜 음성 데이터 실시간 처리
- `Twilio SDK`: 케어콜 전화 발신, 웹소켓 기반 음성 데이터 실시간 처리

## CI/CD 파이프라인

본 프로젝트는 GitHub Actions를 활용하여 CI/CD 파이프라인을 자동화했습니다.

### CI (Continuous Integration)
- **트리거**: 모든 브랜치에 `push` 또는 `pull_request` 이벤트가 발생할 때 워크플로우가 실행됩니다.
- **주요 작업**:
  1. JDK 17 환경을 설정합니다.
  2. `./gradlew clean build` 명령어를 통해 프로젝트를 빌드하고 모든 테스트 코드를 실행합니다.
  3. 이를 통해 코드 변경 사항이 기존 기능에 영향을 주지 않는지 지속적으로 검증합니다.

### CD (Continuous Deployment)
- **트리거**: `dev` 또는 `main` 브랜치에 `push` 이벤트가 발생할 때 워크플로우가 실행됩니다.
- **주요 작업**:
  1. 프로젝트를 빌드하고 Docker 이미지를 생성합니다.
  2. 생성된 이미지를 Docker Hub에 푸시합니다.
  3. AWS EC2 서버에 SSH로 접속하여 최신 Docker 이미지를 pull 받고, 기존 컨테이너를 중지한 후 새로운 컨테이너를 실행하여 애플리케이션을 배포합니다.

## 테스트 및 코드 커버리지

- 모든 핵심 비즈니스 로직에 대해 단위 테스트와 통합 테스트 코드를 작성하여 코드의 안정성과 신뢰성을 확보합니다.
- CI 파이프라인을 통해 모든 Push 및 PR에 대해 전체 테스트가 자동으로 실행되어, 변경 사항으로 인한 잠재적인 버그를 사전에 방지합니다.

<p align="center">
  <img width="962" height="291" alt="스크린샷 2025-08-20 오후 3 27 43" src="https://github.com/user-attachments/assets/6c321aaf-7d5b-41b9-9cab-76d0662803ce" />
</p>

## Git Managing

- **브랜치 컨벤션**: `feat`, `fix`, `refactor`, `test` 등 브랜치 목적에 맞는 prefix를 사용하여 브랜치를 생성합니다.
- **자동 라벨링**: Pull Request가 생성되면, GitHub Actions가 브랜치 이름의 prefix를 분석하여 `enhancement`, `bug`, `refactoring` 등 연관된 라벨을 자동으로 부여합니다. 이를 통해 PR의 목적을 직관적으로 파악하고 체계적으로 관리할 수 있습니다.


## System Specification

### APIs

> 전체 API 명세는 [Notion](https://shrub-crowd-46c.notion.site/Medicare-Call-API-2316196331d28036b150d7f87af402ca?pvs=74)에서 확인하실 수 있습니다.

### Database Schema

<p align="center">
  <img width="2506" height="1877" alt="Untitled (9)" src="https://github.com/user-attachments/assets/cab0c529-0efa-4a90-aecc-4471ed6017b5" />
</p>

- [상세 정보](https://dbdocs.io/contact.terrykim/Medicare-Call-ERD?view=relationships)

### Service Architecture

<p align="center">
  <img width="818" height="424" alt="medicarecall-architecture" src="https://github.com/user-attachments/assets/c94ccd2c-9cf3-474a-8aa8-e7aad41d2546" />
</p>

- [전화 서버 링크](https://github.com/Medicare-Call/Medicare-Call-Telephony-Server)

## 시작하기

### Prerequisites

- Java 17
- Gradle 8.8
- MySQL 8.0
- Redis

### Installation

1. **저장소 복제:**
   ```bash
   git clone https://github.com/your-username/Medicare-Call-Backend.git
   cd Medicare-Call-Backend
   ```
2. **`application.yml` 설정:**
   `src/main/resources/` 경로에 `application.yml` 파일을 생성하고 데이터베이스, Redis, JWT, 외부 API 키 등의 설정을 추가합니다.

3. **애플리케이션 실행:**
   ```bash
   ./gradlew bootRun
   ```

