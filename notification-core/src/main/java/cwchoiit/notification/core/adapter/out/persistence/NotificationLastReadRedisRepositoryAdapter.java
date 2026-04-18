package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationLastReadRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationLastReadRedisRepositoryAdapter implements NotificationLastReadRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LAST_READ_AT_KEY = "user:%d:lastReadAt";

    @Override
    public LocalDateTime recordLastReadAt(Long userId) {
        LocalDateTime lastReadAt = LocalDateTime.now();
        String key = LAST_READ_AT_KEY.formatted(userId);
        redisTemplate.opsForValue().set(key, lastReadAt.toString(), 90, TimeUnit.DAYS);
        return lastReadAt;
    }

    @Override
    public Optional<LocalDateTime> retrieveLastReadAt(Long userId) {
        String key = LAST_READ_AT_KEY.formatted(userId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(LocalDateTime::parse);
    }
}
