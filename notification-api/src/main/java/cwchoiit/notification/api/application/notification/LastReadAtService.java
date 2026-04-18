package cwchoiit.notification.api.application.notification;

import cwchoiit.notification.api.application.port.in.NotificationLastReadUseCase;
import cwchoiit.notification.core.application.port.out.NotificationReadRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastReadAtService implements NotificationLastReadUseCase {

    private final NotificationReadRepository notificationReadRepository;

    @Override
    public LocalDateTime recordLastReadAt(Long userId) {
        return notificationReadRepository.recordLastReadAt(userId);
    }
}
