package cwchoiit.notification.api.application.model;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CommentConvertNotification extends ConvertNotification {
    private final String username;
    private final String userProfileImageUrl;
    private final String comment;
    private final String postImageUrl;

    public CommentConvertNotification(
            String notificationId,
            NotificationType notificationType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String username,
            String userProfileImageUrl,
            String comment,
            String postImageUrl) {
        super(notificationId, notificationType, createdAt, updatedAt);
        this.username = username;
        this.userProfileImageUrl = userProfileImageUrl;
        this.comment = comment;
        this.postImageUrl = postImageUrl;
    }
}
