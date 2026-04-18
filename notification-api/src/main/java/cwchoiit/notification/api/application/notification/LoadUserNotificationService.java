package cwchoiit.notification.api.application.notification;

import cwchoiit.notification.api.application.model.ConvertNotification;
import cwchoiit.notification.api.application.notification.converter.CommentNotificationConverter;
import cwchoiit.notification.api.application.notification.converter.FollowNotificationConverter;
import cwchoiit.notification.api.application.notification.converter.LikeNotificationConverter;
import cwchoiit.notification.api.application.port.in.LoadUserNotificationUseCase;
import cwchoiit.notification.core.application.port.in.NotificationListUseCase;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.FollowNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoadUserNotificationService implements LoadUserNotificationUseCase {
    private final NotificationListUseCase notificationListUseCase;
    private final CommentNotificationConverter commentNotificationConverter;
    private final LikeNotificationConverter likeNotificationConverter;
    private final FollowNotificationConverter followNotificationConverter;

    @Override
    public Slice<ConvertNotification> getUserNotificationsByPivot(
            Long userId, LocalDateTime pivot) {
        return notificationListUseCase
                .findUserNotificationByPivot(userId, pivot)
                .map(
                        notification ->
                                switch (notification.getNotificationType()) {
                                    case LIKE -> likeNotificationConverter.convert(
                                            (LikeNotification) notification);
                                    case FOLLOW -> followNotificationConverter.convert(
                                            (FollowNotification) notification);
                                    case COMMENT -> commentNotificationConverter.convert(
                                            (CommentNotification) notification);
                                });
    }
}
