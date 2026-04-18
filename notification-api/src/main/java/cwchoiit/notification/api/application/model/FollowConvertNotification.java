package cwchoiit.notification.api.application.model;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class FollowConvertNotification extends ConvertNotification {

    private final String username;
    private final String userProfileImageUrl;
    private final boolean isFollowing;

    public FollowConvertNotification(
            String notificationId,
            NotificationType notificationType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String username,
            String userProfileImageUrl,
            boolean isFollowing) {
        super(notificationId, notificationType, createdAt, updatedAt);
        this.username = username;
        this.userProfileImageUrl = userProfileImageUrl;
        this.isFollowing = isFollowing;
    }
}
