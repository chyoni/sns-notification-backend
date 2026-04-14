package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Slice;

public interface NotificationListUseCase {
    // Pivot 기준점: occurredAt, size
    Slice<Notification> findUserNotificationByPivot(Long userId, LocalDateTime occurredAt);
}
