package cwchoiit.notification.core.domain.notification;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
public abstract class Notification {
    private String notificationId;
    private Long userId;
    private NotificationType notificationType;
    private LocalDateTime occurredAt; // 실제 이벤트가 발생한 시간
    private LocalDateTime createdAt; // 데이터베이스에 저장된 시간
    private LocalDateTime expiresAt; // 알림이 데이터베이스에서 삭제될 시간

    protected Notification(Long userId, NotificationType type, LocalDateTime occurredAt) {
        this.userId = userId;
        this.notificationType = type;
        this.occurredAt = occurredAt;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = escalateExpiresAt(occurredAt);
    }

    private LocalDateTime escalateExpiresAt(LocalDateTime occurredAt) {
        return occurredAt.plusDays(90);
    }
}
