package cwchoiit.notification.api.application.port.in;

import java.time.LocalDateTime;

public interface NotificationLastReadUseCase {
    LocalDateTime recordLastReadAt(Long userId);
}
