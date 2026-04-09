package cwchoiit.notification.core.infrastructure.mongo.persistence;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.TypeAlias;

@Getter
@ToString
@TypeAlias("CommentNotification")
public class CommentNotificationMongoEntity extends NotificationMongoEntity {
    private final Long postId;
    private final Long writerId;
    private final Long commentId;
    private final String comment;

    public CommentNotificationMongoEntity(
            String notificationId,
            Long userId,
            Long postId,
            Long writerId,
            Long commentId,
            String comment,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        super(notificationId, userId, NotificationType.COMMENT, occurredAt, createdAt, expiresAt);
        this.postId = postId;
        this.writerId = writerId;
        this.commentId = commentId;
        this.comment = comment;
    }
}
