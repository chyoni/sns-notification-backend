package cwchoiit.notification.consumer.adpater.in.event.follow;

import java.time.LocalDateTime;

public record FollowEvent(
        FollowEventType type, Long userId, Long targetUserId, LocalDateTime createdAt) {}
