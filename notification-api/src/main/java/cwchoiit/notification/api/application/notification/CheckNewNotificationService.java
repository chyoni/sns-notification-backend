package cwchoiit.notification.api.application.notification;

import cwchoiit.notification.api.application.port.in.CheckNewNotificationUseCase;
import cwchoiit.notification.api.application.port.in.NotificationLastReadUseCase;
import cwchoiit.notification.core.application.port.in.NotificationLoadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckNewNotificationService implements CheckNewNotificationUseCase {

    private final NotificationLoadUseCase notificationLoadUseCase;
    private final NotificationLastReadUseCase notificationLastReadUseCase;

    @Override
    public boolean checkNewNotification(Long userId) {
        return notificationLoadUseCase
                .getLatestOccurredAt(userId)
                .map(
                        latestOccurredAt ->
                                notificationLastReadUseCase
                                        .retrieveLastReadAt(userId)
                                        .map(latestOccurredAt::isAfter)
                                        .orElse(true))
                .orElse(false);
    }
}
