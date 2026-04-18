package cwchoiit.notification.api.application.model;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ConvertNotification {
    private String notificationId;
    private NotificationType notificationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
