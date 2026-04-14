package cwchoiit.notification.core.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CommentNotificationTest {

    private static final Long USER_ID = 1L;
    private static final Long POST_ID = 10L;
    private static final Long WRITER_ID = 2L;
    private static final Long COMMENT_ID = 100L;
    private static final String COMMENT = "테스트 댓글입니다.";
    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    // --- create() 팩토리 ---

    @Test
    void create_팩토리_메서드로_생성하면_COMMENT_타입이다() {
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);

        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.COMMENT);
    }

    @Test
    void create_팩토리_메서드로_생성하면_도메인_필드가_올바르게_설정된다() {
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);

        assertThat(notification.getUserId()).isEqualTo(USER_ID);
        assertThat(notification.getPostId()).isEqualTo(POST_ID);
        assertThat(notification.getWriterId()).isEqualTo(WRITER_ID);
        assertThat(notification.getCommentId()).isEqualTo(COMMENT_ID);
        assertThat(notification.getComment()).isEqualTo(COMMENT);
        assertThat(notification.getOccurredAt()).isEqualTo(OCCURRED_AT);
    }

    @Test
    void create_팩토리_메서드로_생성하면_notificationId가_null이다() {
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);

        assertThat(notification.getNotificationId()).isNull();
    }

    @Test
    void create_팩토리_메서드로_생성하면_expiresAt이_occurredAt_기준_90일_후이다() {
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);

        assertThat(notification.getExpiresAt()).isEqualTo(OCCURRED_AT.plusDays(90));
    }

    @Test
    void create_팩토리_메서드로_생성하면_createdAt이_현재_시각으로_설정된다() {
        LocalDateTime before = LocalDateTime.now();
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);
        LocalDateTime after = LocalDateTime.now();

        assertThat(notification.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void 빈_문자열_댓글로도_생성할_수_있다() {
        CommentNotification notification =
                CommentNotification.create(
                        USER_ID, OCCURRED_AT, POST_ID, WRITER_ID, COMMENT_ID, "");

        assertThat(notification.getComment()).isEmpty();
    }

    @Test
    void expiresAt_계산은_occurredAt_값에만_의존한다() {
        LocalDateTime early = LocalDateTime.of(2020, 3, 15, 10, 30);
        LocalDateTime late = LocalDateTime.of(2025, 11, 1, 8, 0);

        CommentNotification n1 =
                CommentNotification.create(USER_ID, early, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);
        CommentNotification n2 =
                CommentNotification.create(USER_ID, late, POST_ID, WRITER_ID, COMMENT_ID, COMMENT);

        assertThat(n1.getExpiresAt()).isEqualTo(early.plusDays(90));
        assertThat(n2.getExpiresAt()).isEqualTo(late.plusDays(90));
    }

    // --- 전체 필드 생성자 ---

    @Test
    void 전체_생성자로_생성하면_모든_필드가_파라미터_값으로_설정된다() {
        String notificationId = "noti-abc";
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 1);
        LocalDateTime expiresAt = LocalDateTime.of(2026, 4, 1, 0, 0);

        CommentNotification notification =
                new CommentNotification(
                        notificationId,
                        USER_ID,
                        OCCURRED_AT,
                        createdAt,
                        expiresAt,
                        POST_ID,
                        WRITER_ID,
                        COMMENT_ID,
                        COMMENT);

        assertThat(notification.getNotificationId()).isEqualTo(notificationId);
        assertThat(notification.getUserId()).isEqualTo(USER_ID);
        assertThat(notification.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
        assertThat(notification.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(notification.getPostId()).isEqualTo(POST_ID);
        assertThat(notification.getWriterId()).isEqualTo(WRITER_ID);
        assertThat(notification.getCommentId()).isEqualTo(COMMENT_ID);
        assertThat(notification.getComment()).isEqualTo(COMMENT);
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.COMMENT);
    }

    @Test
    void 전체_생성자로_생성하면_expiresAt이_자동_계산되지_않고_파라미터_값을_그대로_사용한다() {
        LocalDateTime customExpiresAt = LocalDateTime.of(2099, 12, 31, 23, 59);

        CommentNotification notification =
                new CommentNotification(
                        "noti-1",
                        USER_ID,
                        OCCURRED_AT,
                        LocalDateTime.now(),
                        customExpiresAt,
                        POST_ID,
                        WRITER_ID,
                        COMMENT_ID,
                        COMMENT);

        assertThat(notification.getExpiresAt()).isEqualTo(customExpiresAt);
    }
}
