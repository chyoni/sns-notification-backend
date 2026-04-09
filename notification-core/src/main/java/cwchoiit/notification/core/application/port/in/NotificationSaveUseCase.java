package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;

public interface NotificationSaveUseCase {
    void save(
            Long userId,
            NotificationType type,
            LocalDateTime occurredAt,
            Long postId,
            Long writerId,
            Long commentId,
            String comment);
}
