package cwchoiit.notification.consumer.application.comment;

import cwchoiit.notification.consumer.application.port.in.FollowEventUseCase;
import cwchoiit.notification.core.application.port.in.NotificationLoadUseCase;
import cwchoiit.notification.core.application.port.in.NotificationRemoveUseCase;
import cwchoiit.notification.core.application.port.in.NotificationSaveUseCase;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowEventService implements FollowEventUseCase {

    private final NotificationSaveUseCase notificationSaveUseCase;
    private final NotificationLoadUseCase notificationLoadUseCase;
    private final NotificationRemoveUseCase notificationRemoveUseCase;

    @Override
    public void addFollow(Long userId, Long followerId, LocalDateTime occurredAt) {
        notificationSaveUseCase.saveFollow(NotificationType.FOLLOW, userId, followerId, occurredAt);
    }

    @Override
    public void removeFollow(Long userId, Long followerId, LocalDateTime occurredAt) {
        notificationLoadUseCase
                .findFollowByUserIdAndFollowerId(userId, followerId)
                .ifPresent(notificationRemoveUseCase::removeNotification);
    }
}
