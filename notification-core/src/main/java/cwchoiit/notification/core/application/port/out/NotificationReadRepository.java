package cwchoiit.notification.core.application.port.out;

import java.time.LocalDateTime;

public interface NotificationReadRepository {
    LocalDateTime recordLastReadAt(Long userId);
}
