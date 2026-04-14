package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.FollowNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.infrastructure.mongo.MongoTemplateConfig;
import cwchoiit.notification.core.infrastructure.mongo.persistence.CommentNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.FollowNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.LikeNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.NotificationMongoEntity;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class NotificationMongoRepositoryAdapter implements NotificationRepository {

    private final NotificationMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public NotificationMongoRepositoryAdapter(
            NotificationMongoRepository mongoRepository,
            @Qualifier(MongoTemplateConfig.MONGO_TEMPLATE) MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Notification> findById(String notificationId) {
        return mongoRepository.findById(notificationId).map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationMongoEntity entity = toEntity(notification);
        return mongoRepository.save(Objects.requireNonNull(entity)).toDomain();
    }

    @Override
    public void deleteById(String notificationId) {
        mongoRepository.deleteById(notificationId);
    }

    @Override
    public Optional<Notification> findByComment(Long commentId) {
        return mongoRepository.findByCommentId(commentId).map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Optional<Notification> findLikeByPostId(Long postId) {
        return mongoRepository.findLikeByPostId(postId).map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy) {
        return mongoRepository
                .findLikeByPostIdAndLikedBy(postId, likedBy)
                .map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId) {
        return mongoRepository
                .findFollowByUserIdAndFollowerId(userId, followerId)
                .map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Slice<Notification> findAllByUserIdOrderByOccurredAtDesc(
            Long userId, Pageable pageable) {
        return mongoRepository
                .findAllByUserIdOrderByOccurredAtDesc(userId, pageable)
                .map(NotificationMongoEntity::toDomain);
    }

    @Override
    public Slice<Notification> findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
            Long userId, LocalDateTime occurredAt, Pageable pageable) {
        return mongoRepository
                .findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
                        userId, occurredAt, pageable)
                .map(NotificationMongoEntity::toDomain);
    }

    @Override
    public void addLikeAtomically(
            Long postId, Long postOwnerId, Long likedUserId, LocalDateTime occurredAt) {
        Query query =
                new Query(Criteria.where("postId").is(postId).and("notificationType").is("LIKE"));
        Update upsertUpdate =
                new Update()
                        .addToSet("likedIdsBy", likedUserId)
                        .set("occurredAt", occurredAt)
                        .set("expiresAt", occurredAt.plusDays(90))
                        .setOnInsert("_id", new ObjectId().toString())
                        .setOnInsert("userId", postOwnerId)
                        .setOnInsert("createdAt", LocalDateTime.now())
                        .setOnInsert("notificationType", "LIKE")
                        .setOnInsert("postId", postId)
                        .setOnInsert("_class", "LikeNotification");
        try {
            mongoTemplate.upsert(query, upsertUpdate, "notification");
        } catch (DuplicateKeyException e) {
            // 두 스레드가 동시에 첫 좋아요를 시도할 경우 upsert INSERT 경합 발생.
            // 유니크 인덱스 충돌이 나면 다른 스레드가 이미 INSERT했으므로 순수 update로 재시도.
            Update retryUpdate =
                    new Update()
                            .addToSet("likedIdsBy", likedUserId)
                            .set("occurredAt", occurredAt)
                            .set("expiresAt", occurredAt.plusDays(90));
            mongoTemplate.updateFirst(query, retryUpdate, "notification");
        }
    }

    @Override
    public void removeLikeAtomically(Long postId, Long likedUserId) {
        Query targetQuery =
                new Query(Criteria.where("postId").is(postId).and("notificationType").is("LIKE"));
        mongoTemplate.updateFirst(
                targetQuery, new Update().pull("likedIdsBy", likedUserId), "notification");

        Query emptyArrayQuery =
                new Query(
                        Criteria.where("postId")
                                .is(postId)
                                .and("notificationType")
                                .is("LIKE")
                                .and("likedIdsBy")
                                .size(0));
        mongoTemplate.remove(emptyArrayQuery, "notification");
    }

    private NotificationMongoEntity toEntity(Notification notification) {
        if (notification instanceof CommentNotification commentNotification) {
            return CommentNotificationMongoEntity.from(
                    commentNotification, resolveId(commentNotification.getNotificationId()));
        } else if (notification instanceof LikeNotification likeNotification) {
            return LikeNotificationMongoEntity.from(
                    likeNotification, resolveId(likeNotification.getNotificationId()));
        } else if (notification instanceof FollowNotification followNotification) {
            return FollowNotificationMongoEntity.from(
                    followNotification, resolveId(followNotification.getNotificationId()));
        }
        throw new IllegalArgumentException("Unknown notification type: " + notification.getClass());
    }

    private String resolveId(String id) {
        return id != null ? id : generateId();
    }

    private String generateId() {
        return new ObjectId().toString();
    }
}
