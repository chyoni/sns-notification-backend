package cwchoiit.notification.consumer.adpater.in.event.like.consumer;

import cwchoiit.notification.consumer.adpater.in.event.like.LikeEvent;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LikeEventConsumer {

    @Bean
    public Consumer<LikeEvent> like() {
        return event -> {
            log.info("[like] event: {}", event);
        };
    }
}
