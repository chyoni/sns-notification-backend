package cwchoiit.notification.core.infrastructure.mongo.persistence;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.TypeAlias;

@Getter
@ToString
@TypeAlias("FollowNotification")
public class FollowNotificationMongoEntity extends NotificationMongoEntity {
    private final Long followerId;

    public FollowNotificationMongoEntity(
            String notificationId,
            Long userId,
            Long followerId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        super(notificationId, userId, NotificationType.FOLLOW, occurredAt, createdAt, expiresAt);
        this.followerId = followerId;
    }
}
