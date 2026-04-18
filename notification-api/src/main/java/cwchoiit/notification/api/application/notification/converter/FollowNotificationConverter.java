package cwchoiit.notification.api.application.notification.converter;

import cwchoiit.notification.api.application.model.FollowConvertNotification;
import cwchoiit.notification.commons.application.model.User;
import cwchoiit.notification.commons.application.port.out.UserClientPort;
import cwchoiit.notification.core.domain.notification.FollowNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowNotificationConverter {

    private final UserClientPort userClient;

    public FollowConvertNotification convert(FollowNotification followNotification) {
        User follower = userClient.findUserById(followNotification.getFollowerId()).orElseThrow();
        boolean isFollowing =
                userClient.isFollowing(
                        followNotification.getUserId(), followNotification.getFollowerId());
        return new FollowConvertNotification(
                followNotification.getNotificationId(),
                followNotification.getNotificationType(),
                followNotification.getCreatedAt(),
                followNotification.getOccurredAt(),
                follower.name(),
                follower.profileImageUrl(),
                isFollowing);
    }
}
