package cwchoiit.notification.api.application.notification.converter;

import cwchoiit.notification.api.application.model.CommentConvertNotification;
import cwchoiit.notification.commons.application.model.Post;
import cwchoiit.notification.commons.application.model.User;
import cwchoiit.notification.commons.application.port.out.PostClientPort;
import cwchoiit.notification.commons.application.port.out.UserClientPort;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentNotificationConverter {

    private final UserClientPort userClient;
    private final PostClientPort postClient;

    public CommentConvertNotification convert(CommentNotification commentNotification) {
        User writer = userClient.findUserById(commentNotification.getWriterId()).orElseThrow();
        Post post = postClient.findPostById(commentNotification.getPostId()).orElseThrow();
        return new CommentConvertNotification(
                commentNotification.getNotificationId(),
                commentNotification.getNotificationType(),
                commentNotification.getCreatedAt(),
                commentNotification.getOccurredAt(),
                writer.name(),
                writer.profileImageUrl(),
                commentNotification.getComment(),
                post.imageUrl());
    }
}
