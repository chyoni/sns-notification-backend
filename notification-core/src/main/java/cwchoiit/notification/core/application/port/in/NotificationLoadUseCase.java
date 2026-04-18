package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLoadUseCase {
    Optional<Notification> findNotificationByComment(Long commentId);

    Optional<Notification> findLikeByPostId(Long postId);

    Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy);

    Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId);

    Optional<LocalDateTime> getLatestOccurredAt(Long userId);
}
