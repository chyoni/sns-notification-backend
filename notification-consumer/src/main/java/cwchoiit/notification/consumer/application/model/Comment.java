package cwchoiit.notification.consumer.application.model;

import java.time.LocalDateTime;

public record Comment(Long commentId, String content, LocalDateTime createdAt) {}
