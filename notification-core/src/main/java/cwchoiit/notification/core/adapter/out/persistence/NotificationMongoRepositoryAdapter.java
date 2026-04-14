package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.infrastructure.mongo.persistence.CommentNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.LikeNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.NotificationMongoEntity;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationMongoRepositoryAdapter implements NotificationRepository {

    private final NotificationMongoRepository mongoRepository;

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
    public Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy) {
        return mongoRepository.findLikeByPostIdAndLikedBy(postId, likedBy).map(NotificationMongoEntity::toDomain);
    }

    private NotificationMongoEntity toEntity(Notification notification) {
        if (notification instanceof CommentNotification c) {
            return CommentNotificationMongoEntity.from(c, resolveId(c.getNotificationId()));
        } else if (notification instanceof LikeNotification l) {
            return LikeNotificationMongoEntity.from(l, resolveId(l.getNotificationId()));
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
