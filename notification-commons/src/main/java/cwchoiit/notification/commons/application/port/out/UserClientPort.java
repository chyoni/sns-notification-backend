package cwchoiit.notification.commons.application.port.out;

import cwchoiit.notification.commons.application.model.User;

import java.util.Optional;

public interface UserClientPort {
    Optional<User> findUserById(Long userId);

    boolean isFollowing(Long userId, Long targetUserId);
}
