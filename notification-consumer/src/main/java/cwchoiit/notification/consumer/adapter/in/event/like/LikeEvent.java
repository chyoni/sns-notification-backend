package cwchoiit.notification.consumer.adapter.in.event.like;

import java.time.LocalDateTime;

public record LikeEvent(LikeEventType type, Long postId, Long userId, LocalDateTime createdAt) {}
