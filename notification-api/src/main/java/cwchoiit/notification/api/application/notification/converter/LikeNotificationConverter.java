package cwchoiit.notification.api.application.notification.converter;

import cwchoiit.notification.api.application.model.LikeConvertNotification;
import cwchoiit.notification.commons.application.model.Post;
import cwchoiit.notification.commons.application.model.User;
import cwchoiit.notification.commons.application.port.out.PostClientPort;
import cwchoiit.notification.commons.application.port.out.UserClientPort;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeNotificationConverter {

    private final UserClientPort userClient;
    private final PostClientPort postClient;

    public LikeConvertNotification convert(LikeNotification likeNotification) {
        User lastLikeUser =
                userClient
                        .findUserById(
                                likeNotification
                                        .getLikedIdsBy()
                                        .get(likeNotification.getLikedIdsBy().size() - 1))
                        .orElseThrow();
        Post post = postClient.findPostById(likeNotification.getPostId()).orElseThrow();
        return new LikeConvertNotification(
                likeNotification.getNotificationId(),
                likeNotification.getNotificationType(),
                likeNotification.getCreatedAt(),
                likeNotification.getOccurredAt(),
                lastLikeUser.name(),
                lastLikeUser.profileImageUrl(),
                (long) likeNotification.getLikedIdsBy().size(),
                post.imageUrl());
    }
}
