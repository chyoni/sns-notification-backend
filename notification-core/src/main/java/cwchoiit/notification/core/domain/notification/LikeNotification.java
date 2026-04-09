package cwchoiit.notification.core.domain.notification;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LikeNotification extends Notification {
    private final Long postId;
    /** 좋아요 누른 유저의 ID */
    private final Long likedBy;

    public LikeNotification(
            String notificationId,
            Long userId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            Long postId,
            Long likedBy) {
        super(notificationId, userId, NotificationType.LIKE, occurredAt, createdAt, expiresAt);
        this.postId = postId;
        this.likedBy = likedBy;
    }

    protected LikeNotification(Long userId, LocalDateTime occurredAt, Long postId, Long likedBy) {
        super(userId, NotificationType.LIKE, occurredAt);
        this.postId = postId;
        this.likedBy = likedBy;
    }

    public static LikeNotification create(
            Long userId, LocalDateTime occurredAt, Long postId, Long likedBy) {
        return new LikeNotification(userId, occurredAt, postId, likedBy);
    }
}
