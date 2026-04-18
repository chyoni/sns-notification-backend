package cwchoiit.notification.api.application.model;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class LikeConvertNotification extends ConvertNotification {

    private final String username;
    private final String userProfileImageUrl;
    private final Long userCount;
    private final String postImageUrl;

    public LikeConvertNotification(
            String notificationId,
            NotificationType notificationType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String username,
            String userProfileImageUrl,
            Long userCount,
            String postImageUrl) {
        super(notificationId, notificationType, createdAt, updatedAt);
        this.username = username;
        this.userProfileImageUrl = userProfileImageUrl;
        this.userCount = userCount;
        this.postImageUrl = postImageUrl;
    }
}
