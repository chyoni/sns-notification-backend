package cwchoiit.notification.consumer.application.comment;

import cwchoiit.notification.consumer.application.model.Post;
import cwchoiit.notification.consumer.application.port.in.LikeEventUseCase;
import cwchoiit.notification.consumer.application.port.out.PostClientPort;
import cwchoiit.notification.core.application.port.in.NotificationLoadUseCase;
import cwchoiit.notification.core.application.port.in.NotificationRemoveUseCase;
import cwchoiit.notification.core.application.port.in.NotificationSaveUseCase;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeEventService implements LikeEventUseCase {

    private final PostClientPort postClient;
    private final NotificationSaveUseCase notificationSaveUseCase;
    private final NotificationLoadUseCase notificationLoadUseCase;
    private final NotificationRemoveUseCase notificationRemoveUseCase;

    @Override
    public void addLike(Long postId, Long userId, LocalDateTime createdAt) {
        Post findPost = postClient.findPostById(postId).orElseThrow();
        // 좋아요를 본인 포스트에 본인이 누른 경우
        if (findPost.userId().equals(userId)) {
            return;
        }

        notificationLoadUseCase
                .findLikeByPostId(postId)
                .ifPresentOrElse(
                        notification -> {
                            LikeNotification likeNotification = (LikeNotification) notification;
                            likeNotification.addLikedId(userId, createdAt);
                            notificationSaveUseCase.saveLike(likeNotification);
                        },
                        () ->
                                notificationSaveUseCase.saveLike(
                                        findPost.userId(),
                                        NotificationType.LIKE,
                                        createdAt,
                                        postId,
                                        List.of(userId)));
    }

    @Override
    public void removeLike(Long postId, Long userId) {
        Post findPost = postClient.findPostById(postId).orElseThrow();
        // 좋아요를 본인 포스트에 본인이 누르고 취소한 경우
        if (findPost.userId().equals(userId)) {
            return;
        }

        Notification notification = notificationLoadUseCase.findLikeByPostId(postId).orElseThrow();

        LikeNotification likeNotification = (LikeNotification) notification;
        likeNotification.removeLikedId(userId);

        if (likeNotification.likedCount() == 0) {
            notificationRemoveUseCase.removeNotification(notification);
        } else {
            notificationSaveUseCase.saveLike(likeNotification);
        }
    }
}
