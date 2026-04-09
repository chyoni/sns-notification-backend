package cwchoiit.notification.consumer.application.comment;

import cwchoiit.notification.consumer.application.model.Comment;
import cwchoiit.notification.consumer.application.model.Post;
import cwchoiit.notification.consumer.application.port.in.CommentEventUseCase;
import cwchoiit.notification.consumer.application.port.out.CommentClientPort;
import cwchoiit.notification.consumer.application.port.out.PostClientPort;
import cwchoiit.notification.core.application.port.in.NotificationLoadUseCase;
import cwchoiit.notification.core.application.port.in.NotificationRemoveUseCase;
import cwchoiit.notification.core.application.port.in.NotificationSaveUseCase;
import cwchoiit.notification.core.domain.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentEventService implements CommentEventUseCase {

    private final PostClientPort postClient;
    private final CommentClientPort commentClient;
    private final NotificationSaveUseCase notificationSaveUseCase;
    private final NotificationLoadUseCase notificationLoadUseCase;
    private final NotificationRemoveUseCase notificationRemoveUseCase;

    @Override
    public void addComment(Long postId, Long writerId, Long commentId) {
        Post findPost = postClient.findPostById(postId).orElseThrow();
        if (findPost.userId().equals(writerId)) {
            return;
        }

        Comment findComment = commentClient.findCommentById(commentId).orElseThrow();

        notificationSaveUseCase.save(
                findPost.userId(),
                NotificationType.COMMENT,
                findComment.createdAt(),
                findPost.postId(),
                writerId,
                commentId,
                findComment.content());
    }

    @Override
    public void removeComment(Long postId, Long writerId, Long commentId) {
        Post findPost = postClient.findPostById(postId).orElseThrow();
        if (findPost.userId().equals(writerId)) {
            return;
        }

        notificationLoadUseCase
                .findNotificationByComment(commentId)
                .ifPresentOrElse(
                        notificationRemoveUseCase::removeNotification,
                        () -> log.error("[removeComment] Notification not found"));
    }
}
