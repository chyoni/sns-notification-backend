package cwchoiit.notification.api.application.port.in;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLastReadUseCase {
    LocalDateTime recordLastReadAt(Long userId);

    Optional<LocalDateTime> retrieveLastReadAt(Long userId);
}
