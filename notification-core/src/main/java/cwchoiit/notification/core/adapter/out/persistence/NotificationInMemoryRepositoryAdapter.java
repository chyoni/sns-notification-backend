package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationInMemoryRepositoryAdapter implements NotificationRepository {

    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public Optional<Notification> findById(String notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public Notification save(Notification notification) {
        return notifications.put(notification.getNotificationId(), notification);
    }

    @Override
    public void deleteById(String notificationId) {
        notifications.remove(notificationId);
    }

    @Override
    public Optional<Notification> findByComment(Long commentId) {
        for (Notification value : notifications.values()) {
            if (value.getNotificationType() == NotificationType.COMMENT) {
                CommentNotification commentNotification = (CommentNotification) value;
                if (commentNotification.getCommentId().equals(commentId)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }
}
