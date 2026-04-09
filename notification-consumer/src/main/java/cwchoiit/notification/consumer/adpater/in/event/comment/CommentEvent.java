package cwchoiit.notification.consumer.adpater.in.event.comment;

public record CommentEvent(CommentEventType type, Long postId, Long userId, Long commentId) {}
