package cwchoiit.notification.core.application.port.out;

import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NotificationRepository {
    Optional<Notification> findById(String notificationId);

    Notification save(Notification notification);

    void deleteById(String notificationId);

    Optional<Notification> findByComment(Long commentId);

    Optional<Notification> findLikeByPostId(Long postId);

    Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy);

    Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId);

    Slice<Notification> findAllByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    Slice<Notification> findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
            Long userId, LocalDateTime occurredAt, Pageable pageable);
}
