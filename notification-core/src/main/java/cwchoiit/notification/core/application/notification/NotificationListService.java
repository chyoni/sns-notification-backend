package cwchoiit.notification.core.application.notification;

import cwchoiit.notification.core.application.port.in.NotificationListUseCase;
import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationListService implements NotificationListUseCase {

    private final NotificationRepository notificationRepository;
    private static final int PAGE_SIZE = 20;

    @Override
    public Slice<Notification> findUserNotificationByPivot(Long userId, LocalDateTime occurredAt) {
        if (occurredAt == null) {
            return notificationRepository.findAllByUserIdOrderByOccurredAtDesc(
                    userId, PageRequest.of(0, PAGE_SIZE));
        } else {
            return notificationRepository.findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
                    userId, occurredAt, PageRequest.of(0, PAGE_SIZE));
        }
    }
}
