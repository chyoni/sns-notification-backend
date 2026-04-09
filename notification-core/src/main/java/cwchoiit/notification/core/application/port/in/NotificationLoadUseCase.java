package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.Notification;
import java.util.Optional;

public interface NotificationLoadUseCase {
    Optional<Notification> findNotificationByComment(Long commentId);
}
