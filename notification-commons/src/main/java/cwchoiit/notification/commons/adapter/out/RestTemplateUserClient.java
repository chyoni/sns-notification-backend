package cwchoiit.notification.commons.adapter.out;

import cwchoiit.notification.commons.application.model.User;
import cwchoiit.notification.commons.application.port.out.UserClientPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 유저 서버가 있다고 가정하고, 해당 서버에서 userId로 특정 User를 조회하는 Client */
@Component
public class RestTemplateUserClient implements UserClientPort {
    private final Map<Long, User> users =
            Map.of(
                    1L, new User(1L, "USER_1", "IMAGE_URL_1"),
                    2L, new User(2L, "USER_2", "IMAGE_URL_2"),
                    3L, new User(3L, "USER_3", "IMAGE_URL_3"));

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    /** userId가 targetUserId를 팔로우하는 중인지 판단 */
    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        return true;
    }
}
