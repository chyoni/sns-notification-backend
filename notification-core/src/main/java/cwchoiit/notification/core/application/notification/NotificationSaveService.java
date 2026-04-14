package cwchoiit.notification.core.application.notification;

import cwchoiit.notification.core.application.port.in.NotificationSaveUseCase;
import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSaveService implements NotificationSaveUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public void saveComment(
            Long userId,
            NotificationType type,
            LocalDateTime occurredAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment) {
        if (type == NotificationType.COMMENT) {
            notificationRepository.save(
                    CommentNotification.create(
                            userId, occurredAt, postId, writerId, commentId, comment));
        }
    }

    @Override
    public void saveLike(
            Long userId,
            NotificationType type,
            LocalDateTime occurredAt,
            Long postId,
            List<Long> likedIdsBy) {
        if (type == NotificationType.LIKE) {
            notificationRepository.save(
                    LikeNotification.create(userId, occurredAt, postId, likedIdsBy));
        }
    }

    @Override
    public void saveLike(Notification notification) {
        if (notification.getNotificationType() == NotificationType.LIKE) {
            notificationRepository.save(notification);
        }
    }

    @Override
    public void saveFollow(
            NotificationType type, Long userId, Long followerId, LocalDateTime occurredAt) {
        if (type == NotificationType.FOLLOW) {
            notificationRepository.save(
                    notificationRepository.save(
                            FollowNotification.create(userId, occurredAt, followerId)));
        }
    }
}
