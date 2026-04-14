package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationSaveUseCase {
    void saveComment(
            Long userId,
            NotificationType type,
            LocalDateTime occurredAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment);

    void saveLike(
            Long userId,
            NotificationType type,
            LocalDateTime occurredAt,
            Long postId,
            List<Long> likedBy);

    void saveLike(Notification notification);

    void addLikeAtomically(
            Long postId, Long postOwnerId, Long likedUserId, LocalDateTime occurredAt);

    void saveFollow(NotificationType type, Long userId, Long followerId, LocalDateTime occurredAt);
}
