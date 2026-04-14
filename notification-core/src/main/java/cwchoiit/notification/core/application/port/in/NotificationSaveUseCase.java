package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;

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
            Long likedBy);

    void saveFollow(NotificationType type, Long userId, Long followerId, LocalDateTime occurredAt);
}
