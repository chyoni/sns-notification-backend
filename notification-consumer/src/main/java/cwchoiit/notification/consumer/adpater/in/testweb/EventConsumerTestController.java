package cwchoiit.notification.consumer.adpater.in.testweb;

import cwchoiit.notification.consumer.adpater.in.event.comment.CommentEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventConsumerTestController implements EventConsumerTestControllerSpec {

    private final Consumer<CommentEvent> comment;

    @Override
    @PostMapping("/api/test/comment")
    public void comment(@RequestBody CommentEvent commentEvent) {
        comment.accept(commentEvent);
    }
}
