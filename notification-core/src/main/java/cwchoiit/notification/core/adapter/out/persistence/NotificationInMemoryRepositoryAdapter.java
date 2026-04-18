package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public class NotificationInMemoryRepositoryAdapter implements NotificationRepository {

    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public Optional<Notification> findById(String notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public Notification save(Notification notification) {
        return notifications.put(notification.getNotificationId(), notification);
    }

    @Override
    public void deleteById(String notificationId) {
        notifications.remove(notificationId);
    }

    @Override
    public Optional<Notification> findByComment(Long commentId) {
        for (Notification value : notifications.values()) {
            if (value.getNotificationType() == NotificationType.COMMENT) {
                CommentNotification commentNotification = (CommentNotification) value;
                if (commentNotification.getCommentId().equals(commentId)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Notification> findLikeByPostId(Long postId) {
        for (Notification value : notifications.values()) {
            if (value.getNotificationType() == NotificationType.LIKE) {
                LikeNotification likeNotification = (LikeNotification) value;
                if (likeNotification.getPostId().equals(postId)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Notification> findLikeByPostIdAndLikedBy(Long postId, Long likedBy) {
        for (Notification value : notifications.values()) {
            if (value.getNotificationType() == NotificationType.LIKE) {
                LikeNotification likeNotification = (LikeNotification) value;
                if (likeNotification.getLikedIdsBy().contains(likedBy)
                        && likeNotification.getPostId().equals(postId)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Notification> findFirstByUserIdOrderByOccurredAtDesc(Long userId) {
        return notifications.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .max(Comparator.comparing(Notification::getOccurredAt));
    }

    @Override
    public void addLikeAtomically(
            Long postId, Long postOwnerId, Long likedUserId, LocalDateTime occurredAt) {
        Optional<Notification> existing = findLikeByPostId(postId);
        if (existing.isPresent()) {
            LikeNotification like = (LikeNotification) existing.get();
            like.addLikedId(likedUserId, occurredAt);
        } else {
            String id = UUID.randomUUID().toString();
            LikeNotification newNotification =
                    new LikeNotification(
                            id,
                            postOwnerId,
                            occurredAt,
                            LocalDateTime.now(),
                            occurredAt.plusDays(90),
                            postId,
                            new ArrayList<>(List.of(likedUserId)));
            notifications.put(id, newNotification);
        }
    }

    @Override
    public void removeLikeAtomically(Long postId, Long likedUserId) {
        Optional<Notification> existing = findLikeByPostId(postId);
        if (existing.isPresent()) {
            LikeNotification like = (LikeNotification) existing.get();
            like.removeLikedId(likedUserId);
            if (like.likedCount() == 0) {
                notifications.remove(like.getNotificationId());
            }
        }
    }

    @Override
    public Optional<Notification> findFollowByUserIdAndFollowerId(Long userId, Long followerId) {
        for (Notification value : notifications.values()) {
            if (value.getNotificationType() == NotificationType.FOLLOW) {
                FollowNotification followNotification = (FollowNotification) value;
                if (followNotification.getUserId().equals(userId)
                        && followNotification.getFollowerId().equals(followerId)) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Slice<Notification> findAllByUserIdOrderByOccurredAtDesc(
            Long userId, Pageable pageable) {
        List<Notification> result =
                notifications.values().stream()
                        .filter(n -> n.getUserId().equals(userId))
                        .sorted(Comparator.comparing(Notification::getOccurredAt).reversed())
                        .toList();
        return toSlice(result, pageable);
    }

    @Override
    public Slice<Notification> findAllByUserIdAndOccurredAtLessThanOrderByOccurredAtDesc(
            Long userId, LocalDateTime occurredAt, Pageable pageable) {
        List<Notification> result =
                notifications.values().stream()
                        .filter(
                                n ->
                                        n.getUserId().equals(userId)
                                                && n.getOccurredAt().isBefore(occurredAt))
                        .sorted(Comparator.comparing(Notification::getOccurredAt).reversed())
                        .toList();
        return toSlice(result, pageable);
    }

    private Slice<Notification> toSlice(List<Notification> sorted, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();
        List<Notification> content = sorted.stream().skip(offset).limit(pageSize + 1L).toList();
        boolean hasNext = content.size() > pageSize;
        return new SliceImpl<>(hasNext ? content.subList(0, pageSize) : content, pageable, hasNext);
    }
}
