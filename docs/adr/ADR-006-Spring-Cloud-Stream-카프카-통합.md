# ADR-006: Spring Cloud Stream을 통한 Kafka 통합

| 항목 | 내용 |
|------|------|
| 상태 | 승인됨 (Accepted) |
| 날짜 | 2026-04-09 |
| 의사결정자 | cwchoiit |

## 컨텍스트 (Context)

ADR-002에서 Kafka를 메시지 브로커로 도입하기로 결정했다. 다음 결정은 Spring 기반 애플리케이션에서 **Kafka를 어떻게 통합할 것인가**다.

Spring 생태계에서 Kafka를 사용하는 방법은 크게 세 가지다.

1. **Apache Kafka Client 직접 사용**: `KafkaConsumer` API를 직접 다루는 방식
2. **Spring Kafka (`spring-kafka`)**: `@KafkaListener` 어노테이션 기반의 Spring 통합
3. **Spring Cloud Stream + Kafka Binder**: 메시지 브로커 추상화 위에서 함수형 프로그래밍 모델 사용

## 결정 (Decision)

**Spring Cloud Stream + Kafka Binder** (`spring-cloud-starter-stream-kafka`)를 사용하고, 함수형 프로그래밍 모델을 채택한다.

**함수형 모델 구현:**

`@StreamListener`(레거시 방식) 대신 `java.util.function.Consumer<T>`를 Spring Bean으로 등록한다. Bean 이름이 자동으로 Spring Cloud Stream의 바인딩 이름으로 사용된다.

```java
@Component
@RequiredArgsConstructor
public class CommentEventConsumer {

    private final CommentEventUseCase commentEventUseCase;

    @Bean
    public Consumer<CommentEvent> comment() {  // 바인딩: comment-in-0 → topic: comment
        return event -> {
            if (event.type() == CommentEventType.ADD) {
                commentEventUseCase.addComment(event.postId(), event.userId(), event.commentId());
            } else if (event.type() == CommentEventType.REMOVE) {
                commentEventUseCase.removeComment(event.postId(), event.userId(), event.commentId());
            }
        };
    }
}
```

**바인딩 설정** (`application.yaml`):
```yaml
spring:
  cloud:
    function:
      definition: comment;like;follow
    stream:
      bindings:
        comment-in-0:
          destination: comment
          group: notification-consumer
          consumer.max-attempts: 2
        like-in-0:
          destination: like
          group: notification-consumer
          consumer.max-attempts: 2
        follow-in-0:
          destination: follow
          group: notification-consumer
          consumer.max-attempts: 2
```

**직렬화/역직렬화:**
`content-type: application/json` 설정으로 Spring Cloud Stream이 Jackson을 통해 자동으로 처리한다. 별도의 Deserializer 클래스를 지정하지 않아도 된다.

## 대안 (Alternatives Considered)

**Spring Kafka (`spring-kafka`, `@KafkaListener`):**
- 더 낮은 수준의 Kafka 제어가 가능하다. 파티션, 오프셋, 시크 등 세밀한 설정에 유리하다.
- 그러나 Kafka에 직접 결합되므로 메시지 브로커 교체 시 코드 변경이 필요하다.
- 함수형 모델보다 코드가 장황하다.
- 채택하지 않음.

**Apache Kafka Client 직접 사용:**
- 최대한의 제어권을 제공하지만, Spring 생태계의 DI, 설정, 에러 처리 등을 직접 구현해야 한다.
- 보일러플레이트가 과도하게 증가한다.
- 채택하지 않음.

## 결과 (Consequences)

### 긍정적 결과
- **브로커 추상화**: Kafka Binder를 RabbitMQ Binder로 교체하면 비즈니스 로직 코드 변경 없이 메시지 브로커를 교체할 수 있다.
- **함수형 모델의 간결함**: `Consumer<T>` 하나로 Kafka 토픽 구독 로직이 완성된다.
- **자동 직렬화/역직렬화**: `content-type: application/json` 설정으로 Jackson이 자동 처리한다. `CommentEvent`, `LikeEvent`, `FollowEvent` 레코드를 별도 설정 없이 역직렬화한다.
- **내장 재시도**: `max-attempts: 2` 설정으로 처리 실패 시 재시도가 자동으로 동작한다.

### 부정적 결과 / 트레이드오프
- Spring Cloud Stream의 추상화 레이어로 인해 Kafka 특화 기능(수동 오프셋 관리, 특정 파티션 구독 등)에 접근하기 어렵다.
- 함수형 모델과 바인딩 명명 규칙(`{functionName}-in-{index}`) 학습이 필요하다.
- Spring Cloud 버전(`2023.0.0`)과 Spring Boot 버전(`3.5.7`) 간의 호환성을 별도로 관리해야 한다.

### 후속 과제
- Dead Letter Queue(DLQ) 바인딩 구성: `max-attempts` 초과 메시지를 별도 토픽으로 라우팅
- `LikeEventConsumer`, `FollowEventConsumer`의 비즈니스 로직 구현 (현재 로그 출력 스텁)
- Spring Cloud Stream의 `@DlqDestinationResolver`를 활용한 에러 처리 전략 수립
