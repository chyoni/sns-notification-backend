package cwchoiit.notification.consumer.adpater.out;

import cwchoiit.notification.consumer.application.model.Comment;
import cwchoiit.notification.consumer.application.port.out.CommentClientPort;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 댓글 서버가 있다고 가정하고, 해당 서버에서 commentId로 특정 Comment를 조회하는 Client */
@Component
public class RestTemplateCommentClient implements CommentClientPort {

    private final Map<Long, Comment> comments =
            Map.of(
                    1L, new Comment(1L, "COMMENT_1", LocalDateTime.now()),
                    2L, new Comment(2L, "COMMENT_2", LocalDateTime.now().plusHours(1)),
                    3L, new Comment(3L, "COMMENT_3", LocalDateTime.now().plusMinutes(30)));

    @Override
    public Optional<Comment> findCommentById(Long commentId) {
        return Optional.ofNullable(comments.get(commentId));
    }
}
