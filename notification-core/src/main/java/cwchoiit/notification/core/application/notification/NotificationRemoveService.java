package cwchoiit.notification.core.application.notification;

import cwchoiit.notification.core.application.port.in.NotificationRemoveUseCase;
import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRemoveService implements NotificationRemoveUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public void removeNotification(Notification notification) {
        notificationRepository.deleteById(notification.getNotificationId());
    }
}
