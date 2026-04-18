package cwchoiit.notification.api.application.notification;

import cwchoiit.notification.api.application.port.in.NotificationLastReadUseCase;
import cwchoiit.notification.core.application.port.out.NotificationLastReadRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastReadAtService implements NotificationLastReadUseCase {

    private final NotificationLastReadRepository notificationLastReadRepository;

    @Override
    public LocalDateTime recordLastReadAt(Long userId) {
        return notificationLastReadRepository.recordLastReadAt(userId);
    }

    @Override
    public Optional<LocalDateTime> retrieveLastReadAt(Long userId) {
        return notificationLastReadRepository.retrieveLastReadAt(userId);
    }
}
