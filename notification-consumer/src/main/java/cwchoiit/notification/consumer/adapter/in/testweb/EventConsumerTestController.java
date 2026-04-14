package cwchoiit.notification.consumer.adapter.in.testweb;

import cwchoiit.notification.consumer.adapter.in.event.comment.CommentEvent;
import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEvent;
import cwchoiit.notification.consumer.adapter.in.event.like.LikeEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventConsumerTestController implements EventConsumerTestControllerSpec {

    private final Consumer<CommentEvent> comment;
    private final Consumer<LikeEvent> like;
    private final Consumer<FollowEvent> follow;

    @Override
    @PostMapping("/api/test/comment")
    public void comment(@RequestBody CommentEvent commentEvent) {
        comment.accept(commentEvent);
    }

    @Override
    @PostMapping("/api/test/like")
    public void like(@RequestBody LikeEvent likeEvent) {
        like.accept(likeEvent);
    }

    @Override
    @PostMapping("/api/test/follow")
    public void follow(@RequestBody FollowEvent followEvent) {
        follow.accept(followEvent);
    }
}
