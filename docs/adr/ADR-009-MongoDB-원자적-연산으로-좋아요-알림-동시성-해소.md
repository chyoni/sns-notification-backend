# ADR-009: MongoDB 원자적 연산으로 좋아요 알림 동시성 문제를 해소한다

| 항목 | 내용 |
|------|------|
| 상태 | 수락됨 |
| 날짜 | 2026-04-14 |
| 의사결정자 | cwchoiit |
| 관련 ADR | [ADR-008](ADR-008-좋아요-알림-게시물-단위-집계-단일-도큐먼트-관리.md) — 집계 구조 도입 (이 ADR이 해소하는 후속 과제) |

---

## 배경

[ADR-008](ADR-008-좋아요-알림-게시물-단위-집계-단일-도큐먼트-관리.md)에서 좋아요 알림을 게시물 단위 단일 도큐먼트(`likedIdsBy: List<Long>`)로 집계하는 구조를 도입했다. 당시 `LikeEventService`의 구현은 다음 패턴이었다.

```
findLikeByPostId(postId)   ← 도큐먼트 조회
  → addLikedId(userId)     ← JVM 메모리 내 리스트 수정
  → save(notification)     ← MongoDB replaceOne (전체 도큐먼트 교체)
```

이 read-modify-write 패턴에서는 두 명이 동시에 같은 게시물에 좋아요를 누르면 나중에 `save`한 쪽이 앞서 추가된 ID를 덮어써 **Lost Update**가 발생한다. 또한 도큐먼트가 없는 상태에서 여러 스레드가 동시에 "첫 좋아요"를 처리하면 **중복 도큐먼트**가 생성될 수 있다. ADR-008은 이 문제를 인지하고 "후속 과제"로 명시했으며, 이 ADR에서 해소한다.

---

## 결정

**`MongoRepository.save()` 기반의 read-modify-write 패턴을 제거하고, `MongoTemplate`의 원자적 배열 연산(`$addToSet`, `$pull`, upsert)으로 대체한다.**

구체적으로 세 가지를 결정한다.

### 1. `MongoTemplate` 직접 사용

`MongoRepository`는 저장 시 항상 `replaceOne`(전체 도큐먼트 교체)을 수행하며, `@Query`는 읽기 전용이다. `$addToSet`, `$pull`, `upsert`, `$setOnInsert` 같은 원자적 부분 업데이트는 `MongoTemplate`에서만 표현 가능하다. 따라서 어댑터 레이어(`NotificationMongoRepositoryAdapter`)에 `MongoTemplate`을 추가 주입하고, 원자적 연산이 필요한 메서드에만 사용한다.

### 2. upsert + `DuplicateKeyException` 재시도 패턴

좋아요 추가는 두 경우를 하나의 연산으로 처리한다.

- **도큐먼트가 없는 경우 (첫 좋아요)**: 새 도큐먼트를 생성(`$setOnInsert`)하며 `likedIdsBy` 배열에 userId를 추가
- **도큐먼트가 있는 경우**: `$addToSet`으로 userId를 배열에 추가 (중복 자동 방지), `occurredAt`·`expiresAt`을 최신 시각으로 갱신

다만 upsert 내부에서는 "존재 여부 확인 → 삽입"이 원자적이지 않다. 동시에 여러 스레드가 첫 좋아요를 시도하면 한 스레드만 삽입에 성공하고 나머지는 `DuplicateKeyException`을 받는다(아래 인덱스 참조). 예외를 catch하고 순수 업데이트(`$addToSet`만 수행하는 `updateFirst`)로 재시도하면 모든 userId가 빠짐없이 반영된다.

```
try {
    mongoTemplate.upsert(query, upsertUpdate, "notification");  // 삽입 또는 업데이트
} catch (DuplicateKeyException e) {
    mongoTemplate.updateFirst(query, retryUpdate, "notification");  // 순수 업데이트만 재시도
}
```

### 3. 유니크 부분 인덱스 (`postId`, LIKE 타입)

`postId` 기준 중복 도큐먼트를 DB 레벨에서 차단하기 위해 MongoDB 유니크 부분 인덱스를 생성한다.

```javascript
db.notification.createIndex(
  { "postId": 1 },
  {
    unique: true,
    partialFilterExpression: { "notificationType": "LIKE" },
    name: "uidx_postId_like"
  }
)
```

`partialFilterExpression`으로 LIKE 타입 도큐먼트에만 적용하므로 COMMENT, FOLLOW 도큐먼트에는 영향을 주지 않는다. 인덱스는 `ApplicationReadyEvent` 리스너(`MongoIndexInitializer`)에서 애플리케이션 시작 시 `createIndex()`로 생성한다. `createIndex()`는 인덱스가 이미 존재하면 멱등적으로 동작한다.

### 4. 좋아요 취소: `$pull` + 빈 배열 조건부 삭제

좋아요 취소는 `$pull`로 userId를 원자적으로 제거한 뒤, `likedIdsBy` 크기가 0인 도큐먼트를 별도 쿼리로 삭제한다. 두 연산 사이에 다른 좋아요가 끼어들어도 배열이 비어있지 않으면 삭제되지 않으므로 정합성이 유지된다.

---

## 대안

### `MongoRepository`에서 `@Query` + `@Modifying`으로 처리

Spring Data MongoDB에서 `@Modifying`은 지원되지 않는다(JPA 전용). `MongoRepository`로는 `$addToSet` 같은 부분 업데이트를 선언적으로 표현할 방법이 없다.

**채택하지 않음**: Spring Data MongoDB 스펙상 불가능.

### Pessimistic Lock — `findAndModify` 직렬화

`MongoTemplate.findAndModify()`의 `FindAndModifyOptions.returnNew(true)` 패턴으로 도큐먼트 단위 직렬화를 강제할 수 있다.

- **장점:** 동시성 충돌 없이 순차 처리 보장
- **단점:** 단일 도큐먼트에 대한 쓰기가 직렬화되어 처리량 저하. `$addToSet` upsert 조합으로 충분히 해결 가능한 문제에 비해 과도한 잠금.

**채택하지 않음**: `$addToSet` + `DuplicateKeyException` 재시도 패턴으로 동일 보장을 무잠금으로 달성 가능.

### 애플리케이션 레벨 분산 락 (Redis `SETNX`)

postId별 Redis 키로 락을 잡고 read-modify-write를 직렬화하는 방식.

- **장점:** 기존 `MongoRepository.save()` 코드를 그대로 유지 가능
- **단점:** Redis 추가 네트워크 왕복, 락 TTL 관리 부담, 락 보유 중 프로세스 크래시 시 데드락 위험. MongoDB 자체 원자성으로 해결할 수 있는 문제에 외부 시스템을 도입하는 것은 과설계다.

**채택하지 않음**: MongoDB 원자 연산으로 충분하며 불필요한 복잡도를 추가한다.

---

## 결과

### 긍정적 결과

- **Lost Update 제거**: 동시 좋아요 요청이 모두 `likedIdsBy`에 반영된다. 10개 스레드 동시 호출 통합 테스트(Testcontainers)로 검증됨.
- **중복 도큐먼트 차단**: 유니크 부분 인덱스가 DB 레벨에서 보장하며, `DuplicateKeyException` 재시도로 첫 좋아요 경합도 무손실 처리된다.
- **`LikeEventService` 단순화**: read-modify-write 4단계가 원자적 호출 1줄로 교체되어 비즈니스 로직이 명확해졌다.
- **`$addToSet` 멱등성**: 동일 userId를 여러 번 추가해도 배열 크기가 변하지 않는다.

### 부정적 결과 / 트레이드오프

- **`MongoTemplate` 직접 사용**: `MongoRepository`의 선언적 인터페이스에서 벗어나 저수준 API를 사용한다. 쿼리를 코드로 작성하므로 오타·필드명 불일치가 컴파일 타임에 잡히지 않는다.
- **`DuplicateKeyException` 재시도 패턴의 가시성**: try-catch 안에서 연산 목적이 다른 두 Update 객체를 관리해야 해 코드 가독성이 다소 낮다.
- **좋아요 취소의 비원자성**: `$pull` → 빈 배열 삭제가 두 단계로 나뉜다. 극히 드문 경합 조건에서 빈 배열 도큐먼트가 잠깐 남아 있을 수 있으나, 다음 좋아요 upsert 또는 취소 시 정리되므로 실질적 문제는 없다.
- **인덱스 관리**: `MongoIndexInitializer`가 애플리케이션 시작 시 인덱스를 생성한다. 인덱스 정의 변경 시(예: 이름 변경) 기존 인덱스를 수동으로 drop해야 한다.

### 후속 과제

- `likedIdsBy` 배열이 매우 커지는 인기 게시물에 대한 처리 정책 (ADR-008에서 이어짐): 최근 N명만 보관하거나 별도 컬렉션으로 분리하는 방안 검토
- `MongoTemplate` 쿼리의 필드명 오타를 방지하기 위한 상수 또는 타입 안전 DSL 도입 검토
