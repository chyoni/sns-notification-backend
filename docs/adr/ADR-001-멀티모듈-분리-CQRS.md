# ADR-001: 멀티 모듈 분리 (CQRS 패턴)

| 항목 | 내용 |
|------|------|
| 상태 | 승인됨 (Accepted) |
| 날짜 | 2026-04-09 |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)

MSA 환경에서 댓글 서버, 리액션 서버, 팔로우 서버가 존재하고, 알림 서버는 두 가지 역할을 수행해야 한다.

1. **읽기 (Query)**: 사용자가 자신의 알림 화면을 열어 알림 목록을 조회하는 API 처리
2. **쓰기 (Command)**: 각 서버에서 이벤트를 수신하여 알림 데이터를 생성하는 작업

이 두 역할을 단일 서버가 담당할 경우 다음 문제가 예측된다.

- 알림 화면 조회 요청이 갑자기 급증할 때, 조회 처리로 서버 자원이 소진되면 이벤트 수신 및 저장(쓰기)이 지연되거나 실패한다.
- 반대로 이벤트가 폭발적으로 들어오면 조회 응답 속도가 저하된다.
- 읽기와 쓰기는 부하 특성이 다르므로 독립적으로 스케일링할 수 없다.

## 결정 (Decision)

알림 서버를 세 개의 모듈로 분리한다.

| 모듈 | 역할 | 포트 |
|------|------|------|
| `notification-api` | 사용자 대상 알림 조회 API 전담 (Read) | 8081 |
| `notification-consumer` | Kafka 이벤트 소비 및 알림 데이터 생성 (Write) | 8082 |
| `notification-core` | 도메인, 유스케이스, 포트, 영속성 어댑터 등 공유 라이브러리 | — |

이는 CQRS(Command Query Responsibility Segregation) 패턴을 모듈 수준에서 물리적으로 적용한 것이다.

`notification-core`는 두 서버가 공통으로 사용하는 핵심 비즈니스 로직(엔티티, 유스케이스 포트, 영속성 어댑터)을 담으며, Spring Boot 플러그인이 적용되지 않는 순수 라이브러리 모듈이다.

```
settings.gradle
  include("notification-api", "notification-consumer", "notification-core")

build.gradle (root)
  // notification-core에는 Spring Boot 플러그인 미적용
  if (project.name != "notification-core") {
      apply(plugin = "org.springframework.boot")
  }
```

## 대안 (Alternatives Considered)

**단일 모듈 (Monolith):**
- 개발 편의성은 높으나, 읽기/쓰기를 독립적으로 스케일링할 수 없다.
- 조회 트래픽 급증 시 이벤트 처리에 영향이 전파된다.
- 채택하지 않음.

**완전한 MSA (별도 배포 단위 + 독립 DB):**
- 읽기/쓰기 서버가 완전히 별개의 저장소를 갖고, 데이터를 동기화하는 구조.
- 데이터 일관성 관리, 인프라 복잡도가 학습 프로젝트 규모에 비해 과도하다.
- 채택하지 않음.

## 결과 (Consequences)

### 긍정적 결과
- 읽기/쓰기 서버를 독립적으로 스케일링할 수 있다. 조회 트래픽 급증 시 `notification-api`만 증설하면 된다.
- 한쪽의 장애가 다른 쪽에 전파되지 않는다. `notification-consumer`가 중단되어도 기존 알림 조회는 정상 동작한다.
- `notification-core`를 통해 도메인 로직과 영속성 코드를 재사용한다.

### 부정적 결과 / 트레이드오프
- 빌드 의존성 관리가 필요하다. `notification-core` 변경 시 양쪽 모듈 모두 재빌드해야 한다.
- 현재 두 모듈이 동일한 MongoDB 인스턴스를 공유하므로, 진정한 의미의 읽기/쓰기 저장소 분리는 이루어지지 않았다.

### 후속 과제
- `notification-api`에 실제 알림 조회 REST Controller(Input Adapter) 구현 (현재 `NotificationApiApplication.java`만 존재)
- LIKE, FOLLOW 이벤트 처리 구현 (현재 스텁 상태)
