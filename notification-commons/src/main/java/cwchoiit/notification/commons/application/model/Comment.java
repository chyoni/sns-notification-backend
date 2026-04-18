package cwchoiit.notification.commons.application.model;

import java.time.LocalDateTime;

public record Comment(Long commentId, String content, LocalDateTime createdAt) {}
