package cwchoiit.notification.core.application.port.out;

import cwchoiit.notification.core.domain.notification.Notification;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(String notificationId);

    Notification save(Notification notification);

    void deleteById(String notificationId);

    Optional<Notification> findByComment(Long commentId);

    Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy);

    Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId);
}
