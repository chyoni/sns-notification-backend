package cwchoiit.notification.core.domain.notification;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommentNotification extends Notification {
    private final Long postId;
    private final Long writerId;
    private final Long commentId;
    private final String comment;

    public CommentNotification(
            String notificationId,
            Long userId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment) {
        super(notificationId, userId, NotificationType.COMMENT, occurredAt, createdAt, expiresAt);
        this.postId = postId;
        this.writerId = writerId;
        this.commentId = commentId;
        this.comment = comment;
    }

    protected CommentNotification(
            Long userId,
            LocalDateTime occurredAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment) {
        super(userId, NotificationType.COMMENT, occurredAt);
        this.postId = postId;
        this.writerId = writerId;
        this.commentId = commentId;
        this.comment = comment;
    }

    public static CommentNotification create(
            Long userId,
            LocalDateTime occurredAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment) {
        return new CommentNotification(userId, occurredAt, postId, writerId, commentId, comment);
    }
}
