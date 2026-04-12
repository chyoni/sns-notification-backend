# CLAUDE.md

<!-- ⚠️ 이 파일은 매 세션마다 읽힘 → 200줄 이하 유지, 스킬과 중복 금지 -->

## 프로젝트 개요

- **서비스명**: Notification
- **한 줄 설명**: 알림 서버를 조회 서버와 생성 서버를 나누어 CQRS 패턴을 학습하기 위한 앱
- **주요 기술**: Java 17, Spring Boot 3.x, MySQL 8, Redis, Kafka

## 모듈 구조
```
notification/
    ├── notification-api
    ├── notification-consumer
    └── notification-core
        ├── domain/          # 도메인 정의
        ├── application/     # UseCase(Input Port), Output Port 인터페이스, Service
        ├── adapter/
        │   └── out/         # Output Port 구현체
        └── infrastructure/          
```

## 현재 작업 컨텍스트

<!-- 진행 중인 작업만 기록. -->
<!-- 작업을 완수하면 반드시 아래를 수행한다. -->
<!-- 1. "현재 작업 컨텍스트"에서 완료 항목을 제거한다. -->

## 브랜치 & 커밋 컨벤션

- 브랜치: `feat/기능명`, `fix/이슈명`, `refactor/대상`
- 새 브랜치는 언제나 main에서 생성
- 커밋: `type: 한글 설명` (feat/fix/refactor/test/docs/chore)
- PR 생성 및 Merge 방향은 언제나 브랜치에서 main으로
- 작업 단계마다 WIP 커밋 → 최종 squash merge

