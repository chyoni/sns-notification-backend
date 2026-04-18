package cwchoiit.notification.core.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLastReadRepository {
    LocalDateTime recordLastReadAt(Long userId);

    Optional<LocalDateTime> retrieveLastReadAt(Long userId);
}
