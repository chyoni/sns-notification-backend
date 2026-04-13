package cwchoiit.notification.core.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationLoadServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationLoadService sut;

    // --- findNotificationByComment ---

    @Test
    void 댓글_ID로_알림_조회시_저장된_알림을_반환한다() {
        // given
        Long commentId = 100L;
        CommentNotification expected = 댓글_알림("noti-1", commentId);
        given(notificationRepository.findByComment(commentId)).willReturn(Optional.of(expected));

        // when
        Optional<Notification> result = sut.findNotificationByComment(commentId);

        // then
        assertThat(result).isPresent().contains(expected);
    }

    @Test
    void 댓글_ID로_알림_조회시_알림이_없으면_빈_Optional을_반환한다() {
        // given
        Long commentId = 999L;
        given(notificationRepository.findByComment(commentId)).willReturn(Optional.empty());

        // when
        Optional<Notification> result = sut.findNotificationByComment(commentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 댓글_알림_조회시_Repository의_findByComment를_정확한_파라미터로_한_번만_호출한다() {
        // given
        Long commentId = 100L;
        given(notificationRepository.findByComment(commentId)).willReturn(Optional.empty());

        // when
        sut.findNotificationByComment(commentId);

        // then
        then(notificationRepository).should().findByComment(commentId);
        then(notificationRepository).shouldHaveNoMoreInteractions();
    }

    // --- findLikeByPostIdAndLikedBy ---

    @Test
    void 좋아요_알림_조회시_저장된_알림을_반환한다() {
        // given
        Long postId = 10L;
        Long likedBy = 2L;
        LikeNotification expected = 좋아요_알림("noti-2", postId, likedBy);
        given(notificationRepository.findLikeByPostIdAndLikedBy(postId, likedBy))
                .willReturn(Optional.of(expected));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(postId, likedBy);

        // then
        assertThat(result).isPresent().contains(expected);
    }

    @Test
    void 좋아요_알림_조회시_알림이_없으면_빈_Optional을_반환한다() {
        // given
        Long postId = 10L;
        Long likedBy = 99L;
        given(notificationRepository.findLikeByPostIdAndLikedBy(postId, likedBy))
                .willReturn(Optional.empty());

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(postId, likedBy);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 좋아요_알림_조회시_Repository의_findLikeByPostIdAndLikedBy를_정확한_파라미터로_한_번만_호출한다() {
        // given
        Long postId = 10L;
        Long likedBy = 2L;
        given(notificationRepository.findLikeByPostIdAndLikedBy(postId, likedBy))
                .willReturn(Optional.empty());

        // when
        sut.findLikeByPostIdAndLikedBy(postId, likedBy);

        // then
        then(notificationRepository).should().findLikeByPostIdAndLikedBy(postId, likedBy);
        then(notificationRepository).shouldHaveNoMoreInteractions();
    }

    // --- 픽스처 ---

    private CommentNotification 댓글_알림(String notificationId, Long commentId) {
        return new CommentNotification(
                notificationId,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(90),
                10L,
                2L,
                commentId,
                "테스트 댓글");
    }

    private LikeNotification 좋아요_알림(String notificationId, Long postId, Long likedBy) {
        return new LikeNotification(
                notificationId,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(90),
                postId,
                likedBy);
    }
}
