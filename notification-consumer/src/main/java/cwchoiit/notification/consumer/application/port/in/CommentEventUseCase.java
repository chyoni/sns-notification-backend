package cwchoiit.notification.consumer.application.port.in;

public interface CommentEventUseCase {
    void addComment(Long postId, Long writerId, Long commentId);

    void removeComment(Long postId, Long writerId, Long commentId);
}
