package cwchoiit.notification.consumer.adapter.in.event.comment;

public record CommentEvent(CommentEventType type, Long postId, Long userId, Long commentId) {}
