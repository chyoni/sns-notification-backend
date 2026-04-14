package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.infrastructure.mongo.persistence.NotificationMongoEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotificationMongoRepository
        extends MongoRepository<NotificationMongoEntity, String> {

    @Query("{ 'commentId': ?0 }")
    Optional<NotificationMongoEntity> findByCommentId(Long commentId);

    @Query("{'notificationType': 'LIKE', 'postId':  ?0}")
    Optional<NotificationMongoEntity> findLikeByPostId(Long postId);

    @Query("{'notificationType': 'LIKE', 'postId':  ?0, 'likedIdsBy': ?1}")
    Optional<NotificationMongoEntity> findLikeByPostIdAndLikedBy(Long postId, Long likedBy);

    @Query("{'notificationType': 'FOLLOW', 'userId': ?0, 'followerId': ?1}")
    Optional<NotificationMongoEntity> findFollowByUserIdAndFollowerId(Long userId, Long followerId);

    Slice<NotificationMongoEntity> findAllByUserIdOrderByOccurredAtDesc(
            Long userId, Pageable pageable);

    Slice<NotificationMongoEntity> findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
            Long userId, LocalDateTime occurredAt, Pageable pageable);
}
