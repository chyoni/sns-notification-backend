package cwchoiit.notification.core.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import cwchoiit.notification.core.infrastructure.mongo.persistence.CommentNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.LikeNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.NotificationMongoEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * NotificationMongoRepositoryAdapter의 매핑 로직(toDomain/toEntity) 단위 테스트.
 * private 메서드이므로 public API를 통해 모든 분기를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationMongoRepositoryAdapterMappingTest {

    @Mock private NotificationMongoRepository mongoRepository;
    @InjectMocks private NotificationMongoRepositoryAdapter sut;

    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 1);
    private static final LocalDateTime EXPIRES_AT = OCCURRED_AT.plusDays(90);

    // ============================================================
    // toDomain 분기 검증 (findById / findByComment / findLikeByPostIdAndLikedBy)
    // ============================================================

    @Test
    void COMMENT_엔티티를_조회하면_CommentNotification_도메인으로_변환된다() {
        // given
        CommentNotificationMongoEntity entity =
                댓글_엔티티("noti-1", 1L, 10L, 2L, 100L, "댓글 내용");
        given(mongoRepository.findById("noti-1")).willReturn(Optional.of(entity));

        // when
        Optional<Notification> result = sut.findById("noti-1");

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(CommentNotification.class);
        CommentNotification domain = (CommentNotification) result.get();
        assertThat(domain.getNotificationId()).isEqualTo("noti-1");
        assertThat(domain.getUserId()).isEqualTo(1L);
        assertThat(domain.getPostId()).isEqualTo(10L);
        assertThat(domain.getWriterId()).isEqualTo(2L);
        assertThat(domain.getCommentId()).isEqualTo(100L);
        assertThat(domain.getComment()).isEqualTo("댓글 내용");
        assertThat(domain.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(domain.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(domain.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(domain.getNotificationType()).isEqualTo(NotificationType.COMMENT);
    }

    @Test
    void LIKE_엔티티를_조회하면_LikeNotification_도메인으로_변환된다() {
        // given
        LikeNotificationMongoEntity entity = 좋아요_엔티티("noti-2", 1L, 10L, 2L);
        given(mongoRepository.findById("noti-2")).willReturn(Optional.of(entity));

        // when
        Optional<Notification> result = sut.findById("noti-2");

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(LikeNotification.class);
        LikeNotification domain = (LikeNotification) result.get();
        assertThat(domain.getNotificationId()).isEqualTo("noti-2");
        assertThat(domain.getUserId()).isEqualTo(1L);
        assertThat(domain.getPostId()).isEqualTo(10L);
        assertThat(domain.getLikedBy()).isEqualTo(2L);
        assertThat(domain.getOccurredAt()).isEqualTo(OCCURRED_AT);
        assertThat(domain.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(domain.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(domain.getNotificationType()).isEqualTo(NotificationType.LIKE);
    }

    @Test
    void 미구현_타입인_FOLLOW_엔티티_조회시_빈_Optional을_반환한다() {
        // given - FOLLOW는 아직 구현되지 않아 toDomain()이 null을 반환
        // Optional.map(null)은 Optional.empty()가 됨
        NotificationMongoEntity followEntity = mock(NotificationMongoEntity.class);
        given(followEntity.getNotificationType()).willReturn(NotificationType.FOLLOW);
        given(mongoRepository.findById("follow-1")).willReturn(Optional.of(followEntity));

        // when
        Optional<Notification> result = sut.findById("follow-1");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void ID로_조회시_엔티티가_없으면_빈_Optional을_반환한다() {
        // given
        given(mongoRepository.findById("not-exist")).willReturn(Optional.empty());

        // when
        Optional<Notification> result = sut.findById("not-exist");

        // then
        assertThat(result).isEmpty();
    }

    // ============================================================
    // toEntity 분기 검증 (save)
    // ============================================================

    @Test
    void notificationId가_null인_CommentNotification_저장시_새_ID를_생성하여_MongoDB에_전달한다() {
        // given - create()로 생성하면 notificationId가 null
        CommentNotification domain = CommentNotification.create(1L, OCCURRED_AT, 10L, 2L, 100L, "댓글");
        CommentNotificationMongoEntity savedEntity = 댓글_엔티티("generated-id", 1L, 10L, 2L, 100L, "댓글");
        given(mongoRepository.save(any(CommentNotificationMongoEntity.class))).willReturn(savedEntity);
        ArgumentCaptor<NotificationMongoEntity> captor = ArgumentCaptor.forClass(NotificationMongoEntity.class);

        // when
        sut.save(domain);

        // then
        then(mongoRepository).should().save(captor.capture());
        assertThat(captor.getValue().getNotificationId()).isNotNull().isNotEmpty();
        assertThat(captor.getValue()).isInstanceOf(CommentNotificationMongoEntity.class);
    }

    @Test
    void notificationId가_있는_CommentNotification_저장시_기존_ID를_그대로_사용한다() {
        // given
        CommentNotification domain =
                new CommentNotification("existing-id", 1L, OCCURRED_AT, CREATED_AT, EXPIRES_AT, 10L, 2L, 100L, "댓글");
        CommentNotificationMongoEntity savedEntity = 댓글_엔티티("existing-id", 1L, 10L, 2L, 100L, "댓글");
        given(mongoRepository.save(any(CommentNotificationMongoEntity.class))).willReturn(savedEntity);
        ArgumentCaptor<NotificationMongoEntity> captor = ArgumentCaptor.forClass(NotificationMongoEntity.class);

        // when
        sut.save(domain);

        // then
        then(mongoRepository).should().save(captor.capture());
        assertThat(captor.getValue().getNotificationId()).isEqualTo("existing-id");
    }

    @Test
    void notificationId가_null인_LikeNotification_저장시_새_ID를_생성하여_MongoDB에_전달한다() {
        // given
        LikeNotification domain = LikeNotification.create(1L, OCCURRED_AT, 10L, 2L);
        LikeNotificationMongoEntity savedEntity = 좋아요_엔티티("generated-id", 1L, 10L, 2L);
        given(mongoRepository.save(any(LikeNotificationMongoEntity.class))).willReturn(savedEntity);
        ArgumentCaptor<NotificationMongoEntity> captor = ArgumentCaptor.forClass(NotificationMongoEntity.class);

        // when
        sut.save(domain);

        // then
        then(mongoRepository).should().save(captor.capture());
        assertThat(captor.getValue().getNotificationId()).isNotNull().isNotEmpty();
        assertThat(captor.getValue()).isInstanceOf(LikeNotificationMongoEntity.class);
    }

    @Test
    void notificationId가_있는_LikeNotification_저장시_기존_ID를_그대로_사용한다() {
        // given
        LikeNotification domain =
                new LikeNotification("existing-id", 1L, OCCURRED_AT, CREATED_AT, EXPIRES_AT, 10L, 2L);
        LikeNotificationMongoEntity savedEntity = 좋아요_엔티티("existing-id", 1L, 10L, 2L);
        given(mongoRepository.save(any(LikeNotificationMongoEntity.class))).willReturn(savedEntity);
        ArgumentCaptor<NotificationMongoEntity> captor = ArgumentCaptor.forClass(NotificationMongoEntity.class);

        // when
        sut.save(domain);

        // then
        then(mongoRepository).should().save(captor.capture());
        assertThat(captor.getValue().getNotificationId()).isEqualTo("existing-id");
    }

    @Test
    void CommentNotification_저장_후_반환된_도메인_객체의_필드가_올바르다() {
        // given
        CommentNotification domain =
                new CommentNotification("noti-1", 1L, OCCURRED_AT, CREATED_AT, EXPIRES_AT, 10L, 2L, 100L, "댓글");
        CommentNotificationMongoEntity savedEntity = 댓글_엔티티("noti-1", 1L, 10L, 2L, 100L, "댓글");
        given(mongoRepository.save(any())).willReturn(savedEntity);

        // when
        Notification result = sut.save(domain);

        // then
        assertThat(result).isInstanceOf(CommentNotification.class);
        CommentNotification saved = (CommentNotification) result;
        assertThat(saved.getNotificationId()).isEqualTo("noti-1");
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getCommentId()).isEqualTo(100L);
        assertThat(saved.getComment()).isEqualTo("댓글");
    }

    @Test
    void LikeNotification_저장_후_반환된_도메인_객체의_필드가_올바르다() {
        // given
        LikeNotification domain =
                new LikeNotification("noti-2", 1L, OCCURRED_AT, CREATED_AT, EXPIRES_AT, 10L, 2L);
        LikeNotificationMongoEntity savedEntity = 좋아요_엔티티("noti-2", 1L, 10L, 2L);
        given(mongoRepository.save(any())).willReturn(savedEntity);

        // when
        Notification result = sut.save(domain);

        // then
        assertThat(result).isInstanceOf(LikeNotification.class);
        LikeNotification saved = (LikeNotification) result;
        assertThat(saved.getNotificationId()).isEqualTo("noti-2");
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getPostId()).isEqualTo(10L);
        assertThat(saved.getLikedBy()).isEqualTo(2L);
    }

    // ============================================================
    // deleteById / findByComment / findLikeByPostIdAndLikedBy
    // ============================================================

    @Test
    void deleteById_호출시_mongoRepository의_deleteById가_호출된다() {
        // when
        sut.deleteById("noti-1");

        // then
        then(mongoRepository).should().deleteById("noti-1");
    }

    @Test
    void commentId로_조회시_CommentNotification으로_변환하여_반환한다() {
        // given
        CommentNotificationMongoEntity entity = 댓글_엔티티("noti-1", 1L, 10L, 2L, 100L, "댓글");
        given(mongoRepository.findByCommentId(100L)).willReturn(Optional.of(entity));

        // when
        Optional<Notification> result = sut.findByComment(100L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(CommentNotification.class);
        assertThat(((CommentNotification) result.get()).getCommentId()).isEqualTo(100L);
    }

    @Test
    void commentId로_조회시_엔티티가_없으면_빈_Optional을_반환한다() {
        // given
        given(mongoRepository.findByCommentId(999L)).willReturn(Optional.empty());

        // when
        Optional<Notification> result = sut.findByComment(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void postId와_likedBy로_조회시_LikeNotification으로_변환하여_반환한다() {
        // given
        LikeNotificationMongoEntity entity = 좋아요_엔티티("noti-2", 1L, 10L, 2L);
        given(mongoRepository.findLikeByPostIdAndLikedBy(10L, 2L)).willReturn(Optional.of(entity));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 2L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(LikeNotification.class);
        LikeNotification domain = (LikeNotification) result.get();
        assertThat(domain.getPostId()).isEqualTo(10L);
        assertThat(domain.getLikedBy()).isEqualTo(2L);
    }

    @Test
    void postId와_likedBy로_조회시_엔티티가_없으면_빈_Optional을_반환한다() {
        // given
        given(mongoRepository.findLikeByPostIdAndLikedBy(10L, 99L)).willReturn(Optional.empty());

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 99L);

        // then
        assertThat(result).isEmpty();
    }

    // --- 픽스처 ---

    private CommentNotificationMongoEntity 댓글_엔티티(
            String notificationId,
            Long userId,
            Long postId,
            Long writerId,
            Long commentId,
            String comment) {
        return new CommentNotificationMongoEntity(
                notificationId, userId, postId, writerId, commentId, comment,
                OCCURRED_AT, CREATED_AT, EXPIRES_AT);
    }

    private LikeNotificationMongoEntity 좋아요_엔티티(
            String notificationId, Long userId, Long postId, Long likedBy) {
        return new LikeNotificationMongoEntity(
                notificationId, userId, postId, likedBy,
                OCCURRED_AT, CREATED_AT, EXPIRES_AT);
    }
}
