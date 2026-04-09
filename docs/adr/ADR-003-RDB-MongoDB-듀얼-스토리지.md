# ADR-003: RDB와 MongoDB 듀얼 스토리지 전략

| 항목 | 내용 |
|------|------|
| 상태 | 승인됨 (Accepted) |
| 날짜 | 2026-04-09 |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)

알림 데이터를 저장할 스토리지 기술을 선택해야 한다.

이 프로젝트는 클린 아키텍처로 설계되었으며, 그 핵심 장점 중 하나는 **인프라 교체 유연성**이다. Output Port(`NotificationRepository`) 인터페이스를 통해 영속성 구현체를 격리하므로, 이론적으로 구현체만 교체하면 스토리지 기술을 바꿀 수 있다. 이 유연성이 실제로 동작하는지 직접 검증하고 싶다.

```java
// application.port.out
public interface NotificationRepository {
    Optional<Notification> findById(String notificationId);
    Notification save(Notification notification);
    void deleteById(String notificationId);
    Optional<Notification> findByComment(Long commentId);
}
```

현재 구현체:
- `NotificationMongoRepositoryAdapter` — 프로덕션용 MongoDB 어댑터
- `NotificationInMemoryRepositoryAdapter` — 단위 테스트용 인메모리 더블

## 결정 (Decision)

기본 스토리지는 **RDB(MySQL)**로 가져가되, 의도적으로 **MongoDB와 MySQL 두 가지 스토리지를 모두 구현**하여 클린 아키텍처의 교체 유연성을 직접 체험한다.

**현재 상태 (MongoDB):**
- `spring-boot-data-mongodb` 의존성 적용
- `NotificationMongoRepositoryAdapter`가 `NotificationRepository` Output Port를 구현
- 도메인과의 매핑은 어댑터 내부에서 처리

**예정 (MySQL + JPA):**
- `spring-boot-data-jpa`, `mysql-connector` 의존성 추가
- `NotificationJpaRepositoryAdapter`를 별도로 구현하여 동일한 Output Port를 구현
- 구현체 교체만으로 스토리지가 전환됨을 검증

```
notification-core/
  adapter/out/persistence/
    NotificationMongoRepositoryAdapter.java   // 현재 사용
    NotificationJpaRepositoryAdapter.java     // 예정
    NotificationInMemoryRepositoryAdapter.java // 테스트용
```

## 대안 (Alternatives Considered)

**단일 DB (MongoDB만 사용):**
- 개발 속도는 빠르지만, 클린 아키텍처의 핵심 가치인 교체 유연성을 실증하지 못한다.
- 채택하지 않음.

**단일 DB (MySQL만 사용):**
- RDB는 알림 데이터의 스키마 변동에 유연하지 못하다. 또한 교체 유연성 실증 목적이 사라진다.
- 채택하지 않음.

**ORM 추상화 레이어 (Spring Data 공통 인터페이스):**
- MongoDB와 MySQL의 서로 다른 패러다임(Document vs Relational)을 직접 경험하지 못하고 추상화 뒤에 숨겨진다.
- 채택하지 않음.

## 결과 (Consequences)

### 긍정적 결과
- `NotificationRepository` Port/Adapter 패턴의 실질적 효과를 직접 검증할 수 있다.
- Document 모델(MongoDB)과 관계형 모델(MySQL) 두 패러다임에서의 도메인-엔티티 매핑 경험을 쌓을 수 있다.
- 테스트용 `NotificationInMemoryRepositoryAdapter`가 동일한 인터페이스를 구현하므로, 단위 테스트에서 외부 의존성을 완전히 제거할 수 있다.

### 부정적 결과 / 트레이드오프
- 두 가지 영속성 구현체를 각각 유지보수해야 한다.
- MongoDB 엔티티(`NotificationMongoEntity`)와 JPA 엔티티(`NotificationJpaEntity`)가 별도로 존재하므로 매핑 코드가 중복된다.
- 두 구현체가 동일한 `NotificationRepository` 인터페이스를 구현하면 Spring이 빈을 중복 감지할 수 있으므로, `@Primary`나 프로파일 기반 활성화가 필요하다.

### 후속 과제
- MySQL용 JPA 어댑터(`NotificationJpaRepositoryAdapter`) 구현
- `@Profile` 또는 `@ConditionalOnProperty`를 활용한 어댑터 활성화 전략 결정
- `NotificationMongoRepository` 인터페이스의 패키지 위치 재검토: 현재 `adapter.out.persistence`에 위치하나, Spring Data Repository 인터페이스는 인프라 관심사이므로 `infrastructure.mongo.persistence`로 이동을 고려
