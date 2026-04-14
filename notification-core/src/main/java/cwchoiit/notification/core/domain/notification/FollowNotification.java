package cwchoiit.notification.core.domain.notification;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FollowNotification extends Notification {
    private final Long followerId;

    public FollowNotification(
            String notificationId,
            Long userId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            Long followerId) {
        super(notificationId, userId, NotificationType.FOLLOW, occurredAt, createdAt, expiresAt);
        this.followerId = followerId;
    }

    protected FollowNotification(Long userId, LocalDateTime occurredAt, Long followerId) {
        super(userId, NotificationType.FOLLOW, occurredAt);
        this.followerId = followerId;
    }

    public static FollowNotification create(Long userId, LocalDateTime occurredAt, Long followerId) {
        return new FollowNotification(userId, occurredAt, followerId);
    }
}
