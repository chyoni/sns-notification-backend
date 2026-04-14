package cwchoiit.notification.consumer.adapter.in.event.follow.consumer;

import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEvent;
import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEventType;
import cwchoiit.notification.consumer.application.port.in.FollowEventUseCase;
import java.util.function.Consumer;

import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEventType;
import cwchoiit.notification.consumer.application.port.in.FollowEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventConsumer {

    private final FollowEventUseCase followEventUseCase;

    @Bean
    public Consumer<FollowEvent> follow() {
        return event -> {
            if (event.type() == FollowEventType.ADD) {
                followEventUseCase.addFollow(event.targetUserId(), event.userId(), event.createdAt());
            } else if (event.type() == FollowEventType.REMOVE) {
                followEventUseCase.removeFollow(event.targetUserId(), event.userId(), event.createdAt());
            }
        };
    }
}
