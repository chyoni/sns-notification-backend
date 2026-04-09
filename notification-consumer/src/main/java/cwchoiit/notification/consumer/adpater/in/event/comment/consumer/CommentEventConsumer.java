package cwchoiit.notification.consumer.adpater.in.event.comment.consumer;

import cwchoiit.notification.consumer.adpater.in.event.comment.CommentEvent;
import cwchoiit.notification.consumer.adpater.in.event.comment.CommentEventType;
import cwchoiit.notification.consumer.application.port.in.CommentEventUseCase;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventConsumer {

    private final CommentEventUseCase commentEventUseCase;

    @Bean
    public Consumer<CommentEvent> comment() {
        return event -> {
            if (event.type() == CommentEventType.ADD) {
                commentEventUseCase.addComment(event.postId(), event.userId(), event.commentId());
            } else if (event.type() == CommentEventType.REMOVE) {
                commentEventUseCase.removeComment(
                        event.postId(), event.userId(), event.commentId());
            }
        };
    }
}
