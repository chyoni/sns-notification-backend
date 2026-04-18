package cwchoiit.notification.api.application.port.in;

import cwchoiit.notification.api.application.model.ConvertNotification;
import java.time.LocalDateTime;
import org.springframework.data.domain.Slice;

public interface LoadUserNotificationUseCase {
    Slice<ConvertNotification> getUserNotificationsByPivot(Long userId, LocalDateTime pivot);
}
