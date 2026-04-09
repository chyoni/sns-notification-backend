package cwchoiit.notification.consumer.adpater.in.event.like;

import java.time.LocalDateTime;

public record LikeEvent(LikeEventType type, Long postId, Long userId, LocalDateTime createdAt) {}
