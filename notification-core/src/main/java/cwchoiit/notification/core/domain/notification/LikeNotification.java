package cwchoiit.notification.core.domain.notification;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LikeNotification extends Notification {
    private final Long postId;
    /** 좋아요 누른 유저의 IDs */
    private final List<Long> likedIdsBy;

    public LikeNotification(
            String notificationId,
            Long userId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            Long postId,
            List<Long> likedIdsBy) {
        super(notificationId, userId, NotificationType.LIKE, occurredAt, createdAt, expiresAt);
        this.postId = postId;
        this.likedIdsBy = likedIdsBy;
    }

    protected LikeNotification(
            Long userId, LocalDateTime occurredAt, Long postId, List<Long> likedIdsBy) {
        super(userId, NotificationType.LIKE, occurredAt);
        this.postId = postId;
        this.likedIdsBy = likedIdsBy;
    }

    public static LikeNotification create(
            Long userId, LocalDateTime occurredAt, Long postId, List<Long> likedIdsBy) {
        return new LikeNotification(userId, occurredAt, postId, likedIdsBy);
    }

    public void addLikedId(Long likedBy, LocalDateTime occurredAt) {
        if (!this.getLikedIdsBy().contains(likedBy)) {
            this.getLikedIdsBy().add(likedBy);
            this.updateOccurredAt(occurredAt);
        }
    }

    public void removeLikedId(Long likedBy) {
        this.getLikedIdsBy().remove(likedBy);
    }

    public int likedCount() {
        return this.getLikedIdsBy().size();
    }
}
