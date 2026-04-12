package cwchoiit.notification.consumer.application.port.in;

import java.time.LocalDateTime;

public interface LikeEventUseCase {
    void addLike(Long postId, Long userId, LocalDateTime createdAt);
}
