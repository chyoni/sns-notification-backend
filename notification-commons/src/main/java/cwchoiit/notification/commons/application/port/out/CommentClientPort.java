package cwchoiit.notification.commons.application.port.out;

import cwchoiit.notification.commons.application.model.Comment;

import java.util.Optional;

public interface CommentClientPort {
    Optional<Comment> findCommentById(Long commentId);
}
