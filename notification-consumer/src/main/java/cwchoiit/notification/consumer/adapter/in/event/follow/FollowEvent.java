package cwchoiit.notification.consumer.adapter.in.event.follow;

import java.time.LocalDateTime;

public record FollowEvent(
        FollowEventType type, Long userId, Long targetUserId, LocalDateTime createdAt) {}
