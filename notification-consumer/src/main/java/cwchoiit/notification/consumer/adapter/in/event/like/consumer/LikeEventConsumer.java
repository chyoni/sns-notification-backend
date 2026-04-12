package cwchoiit.notification.consumer.adapter.in.event.like.consumer;

import cwchoiit.notification.consumer.adapter.in.event.like.LikeEvent;
import cwchoiit.notification.consumer.adapter.in.event.like.LikeEventType;
import cwchoiit.notification.consumer.application.port.in.LikeEventUseCase;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventConsumer {

    private final LikeEventUseCase likeEventUseCase;

    @Bean
    public Consumer<LikeEvent> like() {
        return event -> {
            if (event.type() == LikeEventType.ADD) {
                likeEventUseCase.addLike(event.postId(), event.userId(), event.createdAt());
            } else if (event.type() == LikeEventType.REMOVE) {
                likeEventUseCase.removeLike(event.postId(), event.userId());
            }
        };
    }
}
