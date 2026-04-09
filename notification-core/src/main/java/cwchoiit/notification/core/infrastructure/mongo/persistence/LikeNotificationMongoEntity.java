package cwchoiit.notification.core.infrastructure.mongo.persistence;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.TypeAlias;

@Getter
@ToString
@TypeAlias("LikeNotification")
public class LikeNotificationMongoEntity extends NotificationMongoEntity {
    private final Long postId;
    /** 좋아요 누른 유저의 ID */
    private final Long likedBy;

    public LikeNotificationMongoEntity(
            String notificationId,
            Long userId,
            Long postId,
            Long likedBy,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        super(notificationId, userId, NotificationType.LIKE, occurredAt, createdAt, expiresAt);
        this.postId = postId;
        this.likedBy = likedBy;
    }
}
