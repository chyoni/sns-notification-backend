# ADR-007: 알림 목록 조회에 커서 기반 페이지네이션(occurredAt pivot) 을 채택한다

| 항목 | 내용 |
|------|------|
| 상태 | 승인됨 (Accepted) |
| 날짜 | 2026-04-14 |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)

알림 목록은 사용자가 스크롤하며 순차적으로 소비하는 피드 형태의 데이터다. 알림은 실시간으로 생성·삭제되므로, 페이지 번호 기반 오프셋 방식으로 조회하면 데이터가 밀리거나 중복 노출되는 문제가 발생한다. 또한 전체 건수 카운트 쿼리는 대용량 컬렉션에서 비용이 크다. 이를 해결하기 위해 페이지네이션 전략을 명시적으로 결정할 필요가 있다.

## 결정 (Decision)

`occurredAt`(알림 발생 시각)을 커서(pivot)로 사용하는 **Keyset Pagination** 방식을 채택한다.

- Spring Data의 `Slice<T>`를 반환 타입으로 사용한다. `Page<T>`와 달리 count 쿼리를 실행하지 않는다.
- `NotificationListUseCase`는 `occurredAt: LocalDateTime?` 파라미터를 받는다.
  - `null` → 최신 알림부터 첫 페이지 조회 (`findAllByUserIdOrderByOccurredAtDesc`)
  - non-null → 해당 시각보다 이전 알림 조회 (`findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc`)
- 클라이언트는 마지막으로 받은 알림의 `occurredAt` 값을 다음 요청의 커서로 전달한다.
- 페이지 크기는 서버에서 20으로 고정한다.

## 대안 (Alternatives Considered)

### 오프셋 기반 페이지네이션 (`Page<T>`, `?page=N&size=20`)
- 구현이 단순하고 임의 페이지로 이동 가능하다.
- 실시간으로 데이터가 추가·삭제되면 오프셋이 밀려 동일 알림이 두 번 노출되거나 누락된다.
- count 쿼리가 필수이므로 대용량 컬렉션에서 성능 부담이 있다.
- **채택하지 않음**: 알림은 실시간성이 높고 피드 형태의 순차 소비 패턴이므로 부적합하다.

### ObjectId 기반 커서
- MongoDB의 `_id`(ObjectId)는 생성 시각을 포함하므로 정렬 커서로 사용 가능하다.
- 도메인 레이어에 MongoDB 내부 식별자가 노출되어 클린 아키텍처 경계가 오염된다.
- **채택하지 않음**: `occurredAt`은 도메인 개념이므로 경계 오염 없이 동일한 역할을 수행할 수 있다.

## 결과 (Consequences)

### 긍정적 결과
- 실시간 데이터 변경 시에도 커서 이후 데이터만 정확히 조회되어 중복·누락이 없다.
- count 쿼리가 없으므로 대용량 컬렉션에서도 조회 성능이 일정하다.
- `occurredAt` 필드에 인덱스(`userId`, `occurredAt DESC`)를 활용하면 MongoDB가 범위 스캔을 효율적으로 수행한다.

### 부정적 결과 / 트레이드오프
- 특정 페이지 번호로 직접 이동하는 기능을 제공할 수 없다.
- 전체 알림 수를 클라이언트에 제공하려면 별도 count API가 필요하다.
- `occurredAt`이 밀리초 단위로 동일한 알림이 여러 개 존재하면 커서 경계에서 일부 알림이 누락될 수 있다. (현재 서비스 규모에서는 발생 가능성이 낮아 허용한다.)

### 후속 과제
- MongoDB 컬렉션에 `{ userId: 1, occurredAt: -1 }` 복합 인덱스 생성 여부 검토
- `occurredAt` 동일값 중복 문제가 실제로 발생하면 `notificationId`를 보조 커서로 추가하는 방안 고려
