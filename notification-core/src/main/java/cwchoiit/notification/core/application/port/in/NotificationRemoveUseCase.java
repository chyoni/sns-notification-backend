package cwchoiit.notification.core.application.port.in;

import cwchoiit.notification.core.domain.notification.Notification;

public interface NotificationRemoveUseCase {
    void removeNotification(Notification notification);
}
