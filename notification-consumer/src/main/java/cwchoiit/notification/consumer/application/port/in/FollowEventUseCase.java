package cwchoiit.notification.consumer.application.port.in;

import java.time.LocalDateTime;

public interface FollowEventUseCase {
    void addFollow(Long userId, Long followerId, LocalDateTime occurredAt);

    void removeFollow(Long userId, Long followerId, LocalDateTime occurredAt);
}
