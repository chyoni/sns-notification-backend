# Notification Service

알림 서버를 **조회 서버(API)** 와 **생성 서버(Consumer)** 로 분리하여 **CQRS 패턴**을 실습하고, Clean Architecture · Kafka 이벤트 드리븐 · MongoDB 원자적 연산 · Redis 캐싱 등 실무 핵심 기술을 학습하기 위한 포트폴리오 프로젝트.

---

## 목차

- [프로젝트 목적 & 학습 목표](#프로젝트-목적--학습-목표)
- [기술 스택](#기술-스택)
- [모듈 구조](#모듈-구조)
- [시스템 아키텍처](#시스템-아키텍처)
- [핵심 학습 내용](#핵심-학습-내용)
- [도메인 모델](#도메인-모델)
- [데이터 흐름](#데이터-흐름)
- [REST API](#rest-api)
- [실행 방법](#실행-방법)

---

## 프로젝트 목적 & 학습 목표

소셜 서비스의 **"좋아요 / 댓글 / 팔로우" 알림 기능**을 구현하면서 아래 기술 과제를 직접 풀어본다.

| 학습 주제 | 과제 내용 |
|---|---|
| **CQRS** | 알림 생성(Write) 서버와 알림 조회(Read) 서버를 물리적으로 분리 |
| **Clean Architecture** | Adapter → Application → Domain 단방향 의존 강제, Port & Adapter 패턴 |
| **Kafka 이벤트 드리븐** | 좋아요·댓글·팔로우 이벤트를 Kafka 토픽으로 수신해 알림으로 변환 |
| **MongoDB 원자적 연산** | `$addToSet` + Upsert + Partial Unique Index로 동시성 안전 보장 |
| **Redis 캐싱** | 마지막 알림 읽은 시각(lastReadAt)을 Redis에 저장, 신규 알림 여부 판단 |
| **커서 기반 페이지네이션** | `occurredAt`을 pivot으로 사용하는 커서 페이징 구현 |
| **다형성 도큐먼트 설계** | 단일 MongoDB 컬렉션에 @TypeAlias로 알림 타입 구분 |

---

## 기술 스택

```
Language   : Java 17
Framework  : Spring Boot 3.x, Spring Cloud Stream
Database   : MongoDB 7.0 (알림 저장), MySQL 8 (외부 서비스 — 모킹)
Cache      : Redis 7.2 (마지막 읽은 시각)
Messaging  : Apache Kafka
API Docs   : SpringDoc (Swagger UI)
Build      : Gradle (멀티 모듈)
Test       : JUnit 5, Testcontainers
Infra      : Docker Compose
```

---

## 모듈 구조

```
notification/
├── notification-core           # 도메인 & 공통 포트 (Pure Java 라이브러리)
│   ├── domain/                 #   엔티티, 값 객체
│   ├── application/            #   UseCase 인터페이스(Input Port), Output Port, 서비스
│   └── adapter/out/            #   MongoDB / Redis / InMemory 구현체
│       infrastructure/         #   MongoDB 설정, Redis 설정, Index 초기화
│
├── notification-api            # 조회 서버 (포트 8081) — Spring Boot App
│   ├── adapter/in/web/         #   REST 컨트롤러
│   └── application/            #   API 전용 UseCase, 변환 서비스
│
├── notification-consumer       # 이벤트 소비 서버 (포트 8082) — Spring Boot App
│   ├── adapter/in/event/       #   Kafka Consumer (comment / like / follow)
│   └── application/            #   이벤트 처리 서비스
│
└── notification-commons        # 외부 서비스 클라이언트 (공유 라이브러리)
    ├── port/                   #   UserClientPort, PostClientPort, CommentClientPort
    └── adapter/out/client/     #   RestTemplate 기반 모킹 구현체
```

> **핵심 설계 원칙**: `notification-api`와 `notification-consumer` 모두 `notification-core`에 의존하지만, 서로는 의존하지 않는다.
> Write 경로(Consumer)와 Read 경로(API)가 완전히 분리된다.

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    External Systems                         │
│   Post Service  ──┐                                         │
│   Comment Service ├─► Kafka Topics (comment / like / follow)│
│   Follow Service ─┘                                         │
└────────────────────────────┬────────────────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ notification-   │  ← Write 전용 (CQRS Command Side)
                    │   consumer      │
                    │   :8082         │
                    └────────┬────────┘
                             │ MongoDB 원자적 Upsert
                    ┌────────▼────────────────────┐
                    │         MongoDB             │
                    │  Collection: notification   │
                    │  (comment / like / follow)  │
                    └────────┬────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ notification-   │  ← Read 전용 (CQRS Query Side)
                    │     api         │
                    │   :8081         │
                    └────────┬────────┘
                             │ lastReadAt
                    ┌────────▼────────┐
                    │     Redis       │
                    │ user:{id}:      │
                    │ lastReadAt      │
                    └─────────────────┘
```

---

## 핵심 학습 내용

### 1. CQRS — 조회 서버와 쓰기 서버 분리

```
Write Side (notification-consumer)
  └── Kafka Consumer → 이벤트 수신 → 알림 저장 (MongoDB)

Read Side (notification-api)
  └── REST API → 알림 목록 조회 / 신규 알림 여부 확인 (MongoDB + Redis)
```

**분리하면 얻는 것**
- 조회·쓰기 트래픽을 독립적으로 스케일링할 수 있다.
- 읽기 모델을 쓰기 모델과 다르게 최적화할 수 있다 (이 프로젝트에서는 변환 DTO 레이어).
- 한쪽 서버 배포·장애가 다른 쪽에 영향을 주지 않는다.

---

### 2. Clean Architecture — Port & Adapter 패턴

```
Domain (가장 안쪽 — 외부 의존 없음)
  └── Notification, LikeNotification, CommentNotification, FollowNotification

Application (도메인만 의존)
  ├── Input Ports  : NotificationSaveUseCase, NotificationListUseCase, ...
  ├── Output Ports : NotificationRepository, NotificationLastReadRepository
  └── Services     : NotificationSaveService, NotificationListService, ...

Adapter/Out (Application 포트 구현)
  ├── NotificationMongoRepositoryAdapter   ← MongoDB 구현
  ├── NotificationLastReadRedisRepositoryAdapter ← Redis 구현
  └── NotificationInMemoryRepositoryAdapter ← 테스트용 구현

Adapter/In (외부 세계 → Application)
  ├── Web (API): LoadUserNotificationController, CheckNewNotificationController
  └── Event (Consumer): CommentEventConsumer, LikeEventConsumer, FollowEventConsumer
```

**핵심 규칙**: 의존성은 항상 안쪽(도메인)을 향한다. MongoDB나 Redis가 바뀌어도 도메인/서비스 코드는 한 줄도 바꾸지 않는다.

---

### 3. MongoDB — 좋아요 알림 집계 설계

**문제**: 같은 게시물에 좋아요가 100번 오면 알림이 100개가 아닌 **1개에 100명을 집계**해야 한다.

**해결 설계**:

```java
// LikeNotificationMongoEntity
{
  "_id": "...",
  "postId": 42,
  "userId": 11,              // 게시물 작성자 (알림 수신자)
  "notificationType": "LIKE",
  "likedIdsBy": [3, 7, 9],  // 좋아요 누른 사람들 (배열 집계)
  "occurredAt": "2026-04-19T15:30:00",  // 가장 마지막 좋아요 시각
  "expiresAt": "2026-07-18T15:30:00"    // 90일 TTL
}
```

**MongoDB 인덱스 전략**:

```javascript
// 1. 알림 목록 조회 최적화 (정렬 + 커서 페이징)
db.notification.createIndex({ userId: 1, occurredAt: -1 })

// 2. LIKE 타입만 postId에 Partial Unique Index → 문서 1개 보장
db.notification.createIndex(
  { postId: 1 },
  { unique: true, partialFilterExpression: { notificationType: "LIKE" } }
)
```

---

### 4. 동시성 — MongoDB 원자적 Upsert + DuplicateKeyException 핸들링

**시나리오**: 10명이 동시에 같은 게시물에 처음으로 좋아요를 누른다.

**문제점**: "먼저 조회 → 없으면 Insert, 있으면 Update" 패턴은 Race Condition 발생.

**해결**: MongoDB `$addToSet` + Upsert + Fallback 패턴

```java
Update upsertUpdate = new Update()
    .addToSet("likedIdsBy", likedUserId)   // $addToSet → 중복 추가 방지 (멱등)
    .set("occurredAt", occurredAt)
    .setOnInsert("_id", new ObjectId().toString())  // 최초 삽입 시에만 실행
    .setOnInsert("userId", postOwnerId)
    .setOnInsert("_class", "LikeNotification");

try {
    mongoTemplate.upsert(query, upsertUpdate, "notification");
} catch (DuplicateKeyException e) {
    // 두 스레드가 동시에 최초 Insert를 시도했을 때
    // 한 쪽이 Unique Index 위반 → 순수 Update로 재시도
    Update retryUpdate = new Update()
        .addToSet("likedIdsBy", likedUserId)
        .set("occurredAt", occurredAt);
    mongoTemplate.updateFirst(query, retryUpdate, "notification");
}
```

**결과**: 10개의 스레드가 동시에 진입해도 MongoDB 문서는 정확히 1개, `likedIdsBy` 배열에는 10명이 모두 기록된다.

---

### 5. 커서 기반 페이지네이션

**Offset 페이징의 문제점**: `LIMIT 20 OFFSET 100`은 100번째까지 다 읽고 버린다. 데이터가 많을수록 느려진다.

**커서 페이징 설계**:

```
첫 요청: GET /api/v1/notifications/11
  → occurredAt 내림차순으로 최신 20개 반환

두 번째 요청: GET /api/v1/notifications/11?pivot=2026-04-10T14:30:00
  → pivot(마지막으로 본 알림의 occurredAt) 이전 20개만 조회
  → MongoDB: { occurredAt: { $lt: pivot } } 조건으로 바로 인덱스 사용
```

`(userId, occurredAt DESC)` 복합 인덱스와 `$lt` 조건이 결합하여 항상 **O(log n)** 에 조회.

---

### 6. Redis — 신규 알림 여부 판단

**흐름**:
```
PUT /api/v1/notifications/{userId}/read
  → Redis에 user:{userId}:lastReadAt = 현재시각 저장 (TTL 90일)

GET /api/v1/notifications/{userId}/new
  → MongoDB에서 이 사용자의 가장 최근 알림 occurredAt 조회
  → Redis에서 lastReadAt 조회
  → occurredAt > lastReadAt 이면 true (신규 알림 있음)
```

**왜 Redis인가**: `lastReadAt`은 쓰기가 자주 일어나지 않고 읽기가 매우 잦다. 단순 문자열 값 하나이므로 Redis 단일 키로 충분하고, MongoDB 쿼리를 아낄 수 있다.

---

### 7. @TypeAlias — 단일 컬렉션 다형성

세 가지 알림 타입(COMMENT / LIKE / FOLLOW)을 MongoDB 단일 컬렉션 `notification`에 저장.

```java
@TypeAlias("CommentNotification")
public class CommentNotificationMongoEntity extends NotificationMongoEntity { ... }

@TypeAlias("LikeNotification")
public class LikeNotificationMongoEntity extends NotificationMongoEntity { ... }

@TypeAlias("FollowNotification")
public class FollowNotificationMongoEntity extends NotificationMongoEntity { ... }
```

`_class` 필드 대신 `@TypeAlias` 값을 저장해 컬렉션 이름 변경에도 유연하게 대응.  
`MappingMongoConverter`에 `DefaultMongoTypeMapper("")` 설정으로 `_class` 필드를 커스텀 값으로 교체.

---

### 8. Kafka Spring Cloud Stream — 함수형 Consumer 바인딩

```yaml
spring:
  cloud:
    function:
      definition: comment; like; follow;
    stream:
      bindings:
        comment-in-0:
          destination: comment    # Kafka 토픽 이름
          group: notification-consumer
          consumer:
            max-attempts: 2
```

```java
@Bean
public Consumer<CommentEvent> comment() {
    return event -> {
        if (event.type() == CommentEventType.ADD) commentEventUseCase.addComment(...);
        else commentEventUseCase.removeComment(...);
    };
}
```

**함수형 바인딩의 장점**: `@KafkaListener` 어노테이션 없이 순수 `Consumer<T>` 함수로 선언. 테스트 시 함수만 단독 호출 가능, Kafka 의존 없이 단위 테스트 용이.

---

### 9. Self-Interaction Guard — 자기 자신에 대한 알림 방지

```java
// LikeEventService.addLike()
Post post = postClientPort.findPostById(postId);
if (post.userId().equals(userId)) return;  // 자기 게시물 자기가 좋아요 → 알림 생략
```

댓글, 팔로우도 동일 패턴 적용. 비즈니스 로직이 Consumer 서비스 레이어에 위치, Kafka Consumer 자체는 순수 이벤트 라우팅만 담당.

---

## 도메인 모델

```
Notification (abstract)
├── notificationId   String       MongoDB _id
├── userId           Long         알림 수신자
├── notificationType Enum         COMMENT / LIKE / FOLLOW
├── occurredAt       LocalDateTime 마지막 이벤트 발생 시각
├── createdAt        LocalDateTime 알림 최초 생성 시각
└── expiresAt        LocalDateTime occurredAt + 90일 (자동 계산)

CommentNotification extends Notification
├── postId           Long
├── writerId         Long         댓글 작성자
├── commentId        Long
└── comment          String       댓글 내용

LikeNotification extends Notification
├── postId           Long
└── likedIdsBy       List<Long>   좋아요 누른 사용자 ID 목록 (집계)

FollowNotification extends Notification
└── followerId       Long         팔로우한 사용자 ID
```

---

## 데이터 흐름

### Write 경로 (이벤트 → 알림 저장)

```
Kafka Topic (like)
  │  { type: "ADD", postId: 42, userId: 7, createdAt: "..." }
  ▼
LikeEventConsumer.like() — Consumer<LikeEvent>
  ▼
LikeEventService.addLike(postId=42, userId=7, createdAt)
  ├─ PostClientPort.findPostById(42) → Post(postId=42, userId=11)
  ├─ Guard: 7 != 11 → 통과
  └─ NotificationSaveUseCase.addLikeAtomically(postId=42, postOwner=11, liked=7, time)
       ▼
     MongoDB $addToSet upsert on collection "notification"
       { postId: 42, notificationType: "LIKE" }
       → likedIdsBy: [..., 7], occurredAt: updated
```

### Read 경로 (API → 알림 목록 반환)

```
GET /api/v1/notifications/11?pivot=2026-04-10T14:30:00
  ▼
LoadUserNotificationController
  ▼
LoadUserNotificationService.getUserNotificationsByPivot(userId=11, pivot)
  ├─ NotificationListUseCase.findUserNotificationByPivot(11, pivot)
  │    ▼
  │  MongoDB: { userId:11, occurredAt: { $lt: pivot } }
  │           sort(occurredAt DESC) limit(20)
  └─ 각 Notification → Converter → ConvertNotification (username, imageUrl 등 enrichment)
       ▼
     Slice<ConvertNotification> 반환
```

---

## REST API

| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| `GET` | `/api/v1/notifications/{userId}/new` | 마지막으로 읽은 이후 새 알림 있는지 확인 | `Boolean` |
| `GET` | `/api/v1/notifications/{userId}?pivot={datetime}` | 알림 목록 조회 (커서 페이징) | `Slice<ConvertNotification>` |
| `PUT` | `/api/v1/notifications/{userId}/read` | 알림 읽은 시각 기록 | `LocalDateTime` |

**Swagger UI**: `http://localhost:8081/swagger-ui/index.html`

### 응답 예시 — 좋아요 알림

```json
{
  "notificationId": "abc123",
  "notificationType": "LIKE",
  "username": "user_3",
  "userProfileImageUrl": "https://cdn.example.com/3.jpg",
  "userCount": 5,
  "postImageUrl": "https://cdn.example.com/posts/42.jpg",
  "createdAt": "2026-04-15T10:00:00",
  "updatedAt": "2026-04-19T14:30:00"
}
```

---

## 실행 방법

### 사전 요구 사항

- Java 17+
- Docker & Docker Compose

### 1. 인프라 구동

```bash
docker-compose up -d
```

| 서비스 | 주소 |
|--------|------|
| MongoDB | `localhost:27017` |
| Kafka Broker | `localhost:9092` |
| Redis | `localhost:6379` |
| Mongo Express UI | `http://localhost:8888` |

### 2. 서버 실행

**알림 조회 서버 (API)**:
```bash
cd notification-api
./gradlew bootRun --args='--spring.profiles.active=local'
# 또는 환경변수 파일 사용
source .env.local.api && ./gradlew bootRun
```

**알림 생성 서버 (Consumer)**:
```bash
cd notification-consumer
source .env.local.consumer && ./gradlew bootRun
```

### 3. 테스트 실행

```bash
./gradlew test
```

주요 테스트:
- `addLikeAtomically_동시_호출시_모든_userId가_반영된다` — 10개 스레드 동시 좋아요
- `addLikeAtomically_동시_첫_좋아요시_도큐먼트가_하나만_생성된다` — 5개 스레드 최초 좋아요 Race Condition

---

## 프로젝트를 통해 얻은 기술적 인사이트

1. **CQRS는 물리적 분리보다 논리적 분리가 먼저다.** 서버를 나누기 전에 UseCase를 읽기/쓰기로 명확히 나누는 것이 핵심.

2. **MongoDB의 `$addToSet` + Upsert는 집계 패턴의 정석이다.** 좋아요처럼 "여럿을 하나로 모아야 하는" 시나리오에서 RDB보다 훨씬 자연스럽게 원자성을 보장한다.

3. **DuplicateKeyException은 예외가 아니라 예상된 분기다.** 분산 환경에서 동시 삽입 충돌은 항상 일어날 수 있고, Catch → Retry 패턴으로 안전하게 처리할 수 있다.

4. **커서 페이징은 인덱스 설계와 한 쌍이다.** `(userId, occurredAt DESC)` 복합 인덱스가 없으면 커서 조건 `$lt`도 Full Scan이 된다.

5. **Clean Architecture에서 Output Port는 Persistence 추상화다.** 덕분에 InMemory 구현체로 단위 테스트를 짜고, Testcontainers로 통합 테스트를 분리할 수 있다.

6. **Spring Cloud Stream 함수형 바인딩은 Kafka 코드를 순수 Java 함수로 만든다.** `Consumer<T>`가 Kafka를 모르기 때문에 메시지 브로커를 RabbitMQ로 바꿔도 함수 코드는 그대로다.