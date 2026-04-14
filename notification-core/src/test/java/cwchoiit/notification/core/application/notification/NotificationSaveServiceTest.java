package cwchoiit.notification.core.application.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSaveServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationSaveService sut;

    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    // --- saveComment ---

    @Test
    void COMMENT_타입으로_saveComment_호출시_CommentNotification이_저장된다() {
        // when
        sut.saveComment(1L, NotificationType.COMMENT, OCCURRED_AT, 10L, 2L, 100L, "댓글");

        // then
        then(notificationRepository).should().save(any(CommentNotification.class));
    }

    @Test
    void saveComment_호출시_파라미터_값이_CommentNotification에_올바르게_담긴다() {
        // given
        Long userId = 5L;
        Long postId = 20L;
        Long writerId = 3L;
        Long commentId = 200L;
        String comment = "좋은 글이에요";
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // when
        sut.saveComment(
                userId,
                NotificationType.COMMENT,
                OCCURRED_AT,
                postId,
                writerId,
                commentId,
                comment);

        // then
        then(notificationRepository).should().save(captor.capture());
        CommentNotification saved = (CommentNotification) captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPostId()).isEqualTo(postId);
        assertThat(saved.getWriterId()).isEqualTo(writerId);
        assertThat(saved.getCommentId()).isEqualTo(commentId);
        assertThat(saved.getComment()).isEqualTo(comment);
        assertThat(saved.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.COMMENT);
    }

    @ParameterizedTest
    @EnumSource(
            value = NotificationType.class,
            names = {"LIKE", "FOLLOW"})
    void COMMENT_타입이_아니면_saveComment_호출시_저장이_수행되지_않는다(NotificationType type) {
        // when
        sut.saveComment(1L, type, OCCURRED_AT, 10L, 2L, 100L, "댓글");

        // then
        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    void null_타입으로_saveComment_호출시_저장이_수행되지_않는다() {
        // when
        sut.saveComment(1L, null, OCCURRED_AT, 10L, 2L, 100L, "댓글");

        // then
        then(notificationRepository).shouldHaveNoInteractions();
    }

    // --- saveLike ---

    @Test
    void LIKE_타입으로_saveLike_호출시_LikeNotification이_저장된다() {
        // when
        sut.saveLike(1L, NotificationType.LIKE, OCCURRED_AT, 10L, List.of(2L));

        // then
        then(notificationRepository).should().save(any(LikeNotification.class));
    }

    @Test
    void saveLike_호출시_파라미터_값이_LikeNotification에_올바르게_담긴다() {
        // given
        Long userId = 5L;
        Long postId = 20L;
        Long likedBy = 3L;
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        // when
        sut.saveLike(userId, NotificationType.LIKE, OCCURRED_AT, postId, List.of(likedBy));

        // then
        then(notificationRepository).should().save(captor.capture());
        LikeNotification saved = (LikeNotification) captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getPostId()).isEqualTo(postId);
        assertThat(saved.getLikedIdsBy()).containsExactly(likedBy);
        assertThat(saved.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.LIKE);
    }

    @ParameterizedTest
    @EnumSource(
            value = NotificationType.class,
            names = {"COMMENT", "FOLLOW"})
    void LIKE_타입이_아니면_saveLike_호출시_저장이_수행되지_않는다(NotificationType type) {
        // when
        sut.saveLike(1L, type, OCCURRED_AT, 10L, List.of(2L));

        // then
        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    void null_타입으로_saveLike_호출시_저장이_수행되지_않는다() {
        // when
        sut.saveLike(1L, null, OCCURRED_AT, 10L, List.of(2L));

        // then
        then(notificationRepository).shouldHaveNoInteractions();
    }

    // --- addLikeAtomically ---

    @Test
    void addLikeAtomically_호출시_Repository의_addLikeAtomically를_정확한_파라미터로_한_번만_호출한다() {
        // given
        Long postId = 10L;
        Long postOwnerId = 1L;
        Long likedUserId = 2L;

        // when
        sut.addLikeAtomically(postId, postOwnerId, likedUserId, OCCURRED_AT);

        // then
        then(notificationRepository)
                .should()
                .addLikeAtomically(postId, postOwnerId, likedUserId, OCCURRED_AT);
        then(notificationRepository).shouldHaveNoMoreInteractions();
    }
}
