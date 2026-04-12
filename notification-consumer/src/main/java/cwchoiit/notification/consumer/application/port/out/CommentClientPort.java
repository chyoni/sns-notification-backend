package cwchoiit.notification.consumer.application.port.out;

import cwchoiit.notification.consumer.application.model.Comment;
import java.util.Optional;

public interface CommentClientPort {
    Optional<Comment> findCommentById(Long commentId);
}
