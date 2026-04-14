package cwchoiit.notification.core.application.notification;

import cwchoiit.notification.core.application.port.in.NotificationLoadUseCase;
import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.Notification;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLoadService implements NotificationLoadUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public Optional<Notification> findNotificationByComment(Long commentId) {
        return notificationRepository.findByComment(commentId);
    }

    @Override
    public Optional<Notification> findLikeByPostId(Long postId) {
        return notificationRepository.findLikeByPostId(postId);
    }

    @Override
    public Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy) {
        return notificationRepository.findLikeByPostIdAndLikedBy(postId, likedBy);
    }

    @Override
    public Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId) {
        return notificationRepository.findFollowByUserIdAndFollowerId(userId, followerId);
    }
}
