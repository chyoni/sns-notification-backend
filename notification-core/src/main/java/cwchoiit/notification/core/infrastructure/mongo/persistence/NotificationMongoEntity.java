package cwchoiit.notification.core.infrastructure.mongo.persistence;

import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Getter
@ToString
@Document("notification")
public abstract class NotificationMongoEntity {
    @Id
    @Field(targetType = FieldType.STRING)
    private final String notificationId;

    private final Long userId;
    private final NotificationType notificationType;
    private final LocalDateTime occurredAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    protected NotificationMongoEntity(
            String notificationId,
            Long userId,
            NotificationType notificationType,
            LocalDateTime occurredAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
