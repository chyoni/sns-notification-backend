# 클린 아키텍처 리뷰 — Notification Server

> 리뷰 기준: Robert C. Martin의 클린 아키텍처 **의존성 규칙(Dependency Rule)**
> — 모든 소스 코드 의존성은 안쪽(Domain 방향)으로만 흐를 것

---

## 1. 전체 의존성 방향 다이어그램

```
┌────────────────────────────────────────────────────────┐
│               [notification-api]                       │
│   (Input Adapter: REST Controller) ─────────────┐     │
└───────────────────────┬────────────────────────  │     │
                        │ uses                     │     │
┌───────────────────────▼────────────────────────  │     │
│            [notification-consumer]               │     │
│   (Input Adapter: Kafka Consumer)  ──────────────┤     │
│   (Output Adapter: External Client) ─────────────┤     │
└───────────────────────┬────────────────────────  │     │
                        │ uses                     │     │
┌───────────────────────▼─────────────────────────────┐  │
│                 [notification-core]                  │  │
│                                                      │  │
│  Infrastructure ──► Adapter Out ──► Application ──► Domain
│                                          ▲           │  │
│                              Port Out ───┘           │  │
│                              Port In  ───────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 레이어별 분석

### 2.1 Domain 레이어

**위치:** `notification-core/.../domain/notification/`

| 파일 | 역할 |
|------|------|
| `Notification.java` | 추상 기반 도메인 엔티티. `expiresAt` 계산 비즈니스 규칙 내재화 |
| `CommentNotification.java` | 댓글 알림 엔티티. 팩토리 메서드 `create()` 제공 |
| `LikeNotification.java` | 좋아요 알림 엔티티. 팩토리 메서드 `create()` 제공 |
| `NotificationType.java` | 도메인 열거형: LIKE, COMMENT, FOLLOW |

**평가: CLEAN ✅**

- 프레임워크 의존성 없음 (Spring, JPA, MongoDB 등 0건)
- Lombok(`@Getter`, `@ToString`, `@AllArgsConstructor`)만 사용 — 컴파일 타임 코드 생성 도구로 허용
- 비즈니스 규칙(`escalateExpiresAt`: 90일 만료)이 도메인 안에 캡슐화됨
- 팩토리 메서드 패턴으로 객체 생성 의도 명확

---

### 2.2 Application 레이어

**위치:** `notification-core/.../application/`

#### Input Ports (UseCase 인터페이스)

| 파일 | 메서드 |
|------|--------|
| `NotificationSaveUseCase` | `save(userId, type, occurredAt, postId, writerId, commentId, comment)` |
| `NotificationLoadUseCase` | `findNotificationByComment(commentId): Optional<Notification>` |
| `NotificationRemoveUseCase` | `removeNotification(notification)` |

**평가: CLEAN ✅** — 순수 Java 인터페이스, 프레임워크 의존 없음

#### Output Port

| 파일 | 메서드 |
|------|--------|
| `NotificationRepository` | `findById`, `save`, `deleteById`, `findByComment` |

**평가: CLEAN ✅** — 순수 Java 인터페이스, 영속성 기술 무관

#### Application Services

| 파일 | 구현 인터페이스 |
|------|----------------|
| `NotificationSaveService` | `NotificationSaveUseCase` |
| `NotificationLoadService` | `NotificationLoadUseCase` |
| `NotificationRemoveService` | `NotificationRemoveUseCase` |

**평가: 경미한 위반 ⚠️**

```java
import org.springframework.stereotype.Service; // ← Spring 의존

@Service  // ← Application 레이어에서 프레임워크 어노테이션 사용
@RequiredArgsConstructor
public class NotificationSaveService implements NotificationSaveUseCase { ... }
```

Spring DI 컨테이너 등록을 위한 최소한의 의존으로, 실용적 트레이드오프로 허용한다.
순수 클린 아키텍처라면 `@Configuration` 클래스에서 `@Bean`으로 수동 등록해야 하지만, 보일러플레이트 증가 대비 실익이 작다.

---

### 2.3 Adapter 레이어

#### Core 모듈 — Output Adapter

**위치:** `notification-core/.../adapter/out/persistence/`

| 파일 | 역할 | 평가 |
|------|------|------|
| `NotificationMongoRepositoryAdapter` | `NotificationRepository` 구현. MongoDB 어댑터 | CLEAN ✅ |
| `NotificationInMemoryRepositoryAdapter` | `NotificationRepository` 구현. 테스트용 인메모리 더블 | CLEAN ✅ |
| `NotificationMongoRepository` | Spring Data `MongoRepository<NotificationMongoEntity, String>` | 위치 검토 필요 ⚠️ |

`NotificationMongoRepository`는 Spring Data Repository 인터페이스로 인프라 관심사다. `adapter.out.persistence`가 아닌 `infrastructure.mongo.persistence`에 위치하는 것이 레이어 의도에 더 부합한다.

#### Consumer 모듈 — Input Adapter

**위치:** `notification-consumer/.../adpater/in/` (**오타 주의: `adpater`**)

| 파일 | 역할 | 평가 |
|------|------|------|
| `CommentEventConsumer` | Kafka 이벤트 수신 → `CommentEventUseCase` 위임 | CLEAN ✅ |
| `LikeEventConsumer` | Kafka 이벤트 수신 → **스텁 (로그 출력만)** | 미구현 ⚠️ |
| `FollowEventConsumer` | Kafka 이벤트 수신 → **스텁 (로그 출력만)** | 미구현 ⚠️ |
| `EventConsumerTestController` | HTTP → Kafka Consumer 직접 호출 (수동 테스트용) | CLEAN ✅ |

#### Consumer 모듈 — Output Adapter

**위치:** `notification-consumer/.../adpater/out/`

| 파일 | 구현 인터페이스 | 평가 |
|------|----------------|------|
| `RestTemplateCommentClient` | `CommentClientPort` | 현재 인메모리 스텁 구현 ✅ |
| `RestTemplatePostClient` | `PostClientPort` | 현재 인메모리 스텁 구현 ✅ |

---

### 2.4 Infrastructure 레이어

**위치:** `notification-core/.../infrastructure/mongo/`

| 파일 | 역할 | 평가 |
|------|------|------|
| `MongoConfig` | MongoDB 클라이언트 설정 | CLEAN ✅ |
| `MongoTemplateConfig` | `MongoTemplate` 빈 설정 | CLEAN ✅ |
| `MongoProperties` | `notification.mongo.uri` 프로퍼티 바인딩 | CLEAN ✅ |
| `NotificationMongoEntity` | 추상 MongoDB 도큐먼트 엔티티 | CLEAN ✅ |
| `LikeNotificationMongoEntity` | LIKE 알림 MongoDB 엔티티 | CLEAN ✅ |
| `CommentNotificationMongoEntity` | COMMENT 알림 MongoDB 엔티티 | CLEAN ✅ |

**평가: CLEAN ✅**

MongoDB 엔티티들이 `NotificationType`(Domain 열거형)을 참조하는 것은 올바른 inward 의존성 방향이다.

---

## 3. 의존성 방향 검증

| 의존 관계 | 방향 | 판정 |
|-----------|------|------|
| `Infrastructure` → `Domain` (NotificationType 참조) | inward ✅ | CLEAN |
| `Adapter Out` → `Application Port Out` (implements) | inward ✅ | CLEAN |
| `Application Service` → `Application Port Out` (uses) | inward ✅ | CLEAN |
| `Application Service` → `Domain` (uses) | inward ✅ | CLEAN |
| `Adapter In (consumer)` → `Application Port In` (uses) | inward ✅ | CLEAN |
| `Application Service` → `Spring @Service` | outward ⚠️ | 허용 |
| `Adapter Out` → `Infrastructure Entity` | same/outer ✅ | CLEAN |

---

## 4. 발견 사항 요약

| # | 구분 | 내용 | 심각도 | 권장 조치 |
|---|------|------|--------|-----------|
| 1 | 패키지 오타 | `adpater` → `adapter` (consumer 모듈 전체) | **중간** | 리팩토링으로 일괄 수정 |
| 2 | 패키지 배치 | `NotificationMongoRepository`가 `adapter.out.persistence`에 위치 | **낮음** | `infrastructure.mongo.persistence`로 이동 고려 |
| 3 | 프레임워크 의존 | Application Service의 `@Service` | **낮음** | 실용적 트레이드오프로 허용 |
| 4 | 미구현 | LIKE, FOLLOW 이벤트 처리 스텁 | **중간** | 구현 필요 (`toDomain()`/`toEntity()` NPE 위험) |
| 5 | Input Adapter 부재 | `notification-api`에 REST Controller 없음 | **높음** | 알림 조회 API 구현 필요 |
| 6 | 테스트 패턴 | `@DynamicPropertySource` 미사용 (`System.setProperty` 사용 중) | **낮음** | Spring 권장 방식으로 교체 고려 |

---

## 5. 개선 권장 사항

### 즉시 수정 권장 (이슈 #1, #4)

**이슈 #1 — 패키지 오타 수정:**
```
notification-consumer/.../adpater/ → adapter/
```
IDE의 리팩토링 기능으로 패키지 이름을 일괄 수정한다. import 구문도 자동으로 갱신된다.

**이슈 #4 — NPE 위험 제거:**
```java
// NotificationMongoRepositoryAdapter.java
private Notification toDomain(NotificationMongoEntity entity) {
    if (...COMMENT...) { return new CommentNotification(...); }
    // TODO: LIKE, FOLLOW
    return null; // ← NPE 위험
}
```
LIKE, FOLLOW 타입에 대한 매핑을 구현하거나, `null` 반환 대신 `Optional`을 반환하도록 시그니처를 변경한다.

### 단기 검토 권장 (이슈 #2, #5)

**이슈 #2 — `NotificationMongoRepository` 이동:**
Spring Data Repository 인터페이스는 인프라 관심사이므로 `infrastructure.mongo.persistence` 패키지로 이동을 고려한다.

**이슈 #5 — REST Controller 구현:**
`notification-api`에 `NotificationLoadUseCase`를 주입받는 `NotificationController`를 추가한다.

### 낮은 우선순위 (이슈 #3, #6)

**이슈 #3**: `@Service` 사용은 현재 수준에서 실용적 트레이드오프로 허용한다.

**이슈 #6 — `@DynamicPropertySource` 전환:**
```java
// Before
static { System.setProperty("notification.mongo.uri", mongoDb.getReplicaSetUrl("notification")); }

// After
@DynamicPropertySource
static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("notification.mongo.uri", () -> mongoDb.getReplicaSetUrl("notification"));
}
```
