package cwchoiit.notification.consumer.adapter.in.event.follow.consumer;

import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEvent;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FollowEventConsumer {

    @Bean
    public Consumer<FollowEvent> follow() {
        return event -> {
            log.info("[follow] event: {}", event);
        };
    }
}
