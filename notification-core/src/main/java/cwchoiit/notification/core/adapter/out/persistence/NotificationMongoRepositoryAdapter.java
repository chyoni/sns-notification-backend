package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
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
        return mongoRepository.findById(notificationId).map(this::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationMongoEntity entity = toEntity(notification);
        return toDomain(mongoRepository.save(Objects.requireNonNull(entity)));
    }

    @Override
    public void deleteById(String notificationId) {
        mongoRepository.deleteById(notificationId);
    }

    @Override
    public Optional<Notification> findByComment(Long commentId) {
        return mongoRepository.findByCommentId(commentId).map(this::toDomain);
    }

    private Notification toDomain(NotificationMongoEntity entity) {
        if (entity.getNotificationType() == NotificationType.COMMENT
                && entity instanceof CommentNotificationMongoEntity commentEntity) {
            return new CommentNotification(
                    commentEntity.getNotificationId(),
                    commentEntity.getUserId(),
                    commentEntity.getOccurredAt(),
                    commentEntity.getCreatedAt(),
                    commentEntity.getExpiresAt(),
                    commentEntity.getPostId(),
                    commentEntity.getWriterId(),
                    commentEntity.getCommentId(),
                    commentEntity.getComment());
        } else if (entity.getNotificationType() == NotificationType.LIKE
                && entity instanceof LikeNotificationMongoEntity likeEntity) {
            return new LikeNotification(
                    likeEntity.getNotificationId(),
                    likeEntity.getUserId(),
                    likeEntity.getOccurredAt(),
                    likeEntity.getCreatedAt(),
                    likeEntity.getExpiresAt(),
                    likeEntity.getPostId(),
                    likeEntity.getLikedBy());
        }
        // TODO: FOLLOW
        return null;
    }

    private NotificationMongoEntity toEntity(Notification notification) {
        if (notification.getNotificationType() == NotificationType.COMMENT
                && notification instanceof CommentNotification commentNotification) {
            if (commentNotification.getNotificationId() == null) {
                return new CommentNotificationMongoEntity(
                        generateId(),
                        commentNotification.getUserId(),
                        commentNotification.getPostId(),
                        commentNotification.getWriterId(),
                        commentNotification.getCommentId(),
                        commentNotification.getComment(),
                        commentNotification.getOccurredAt(),
                        commentNotification.getCreatedAt(),
                        commentNotification.getExpiresAt());
            } else {
                return new CommentNotificationMongoEntity(
                        commentNotification.getNotificationId(),
                        commentNotification.getUserId(),
                        commentNotification.getPostId(),
                        commentNotification.getWriterId(),
                        commentNotification.getCommentId(),
                        commentNotification.getComment(),
                        commentNotification.getOccurredAt(),
                        commentNotification.getCreatedAt(),
                        commentNotification.getExpiresAt());
            }
        } else if (notification.getNotificationType() == NotificationType.LIKE
                && notification instanceof LikeNotification likeNotification) {
            if (likeNotification.getNotificationId() == null) {
                return new LikeNotificationMongoEntity(
                        generateId(),
                        likeNotification.getUserId(),
                        likeNotification.getPostId(),
                        likeNotification.getLikedBy(),
                        likeNotification.getOccurredAt(),
                        likeNotification.getCreatedAt(),
                        likeNotification.getExpiresAt());
            } else {
                return new LikeNotificationMongoEntity(
                        likeNotification.getNotificationId(),
                        likeNotification.getUserId(),
                        likeNotification.getPostId(),
                        likeNotification.getLikedBy(),
                        likeNotification.getOccurredAt(),
                        likeNotification.getCreatedAt(),
                        likeNotification.getExpiresAt());
            }
        }
        // TODO: FOLLOW
        return null;
    }

    private String generateId() {
        return new ObjectId().toString();
    }
}
