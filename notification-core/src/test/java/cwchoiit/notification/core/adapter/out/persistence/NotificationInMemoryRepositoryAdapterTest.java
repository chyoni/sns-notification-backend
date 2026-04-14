package cwchoiit.notification.core.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationInMemoryRepositoryAdapterTest {

    private NotificationInMemoryRepositoryAdapter sut;
    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        sut = new NotificationInMemoryRepositoryAdapter();
    }

    // --- save / findById ---

    @Test
    void 저장한_알림을_ID로_조회할_수_있다() {
        // given
        LikeNotification notification = 좋아요_알림("noti-1", 10L, 2L);
        sut.save(notification);

        // when
        Optional<Notification> result = sut.findById("noti-1");

        // then
        assertThat(result).isPresent().contains(notification);
    }

    @Test
    void 처음_저장시_save의_반환값은_null이다() {
        // given - ConcurrentHashMap.put()은 이전 값을 반환하므로 첫 저장은 null 반환
        LikeNotification notification = 좋아요_알림("noti-1", 10L, 2L);

        // when
        Notification result = sut.save(notification);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 같은_ID로_재저장시_이전_알림을_반환한다() {
        // given
        LikeNotification first = 좋아요_알림("noti-1", 10L, 2L);
        LikeNotification second = 좋아요_알림("noti-1", 20L, 3L);
        sut.save(first);

        // when
        Notification result = sut.save(second);

        // then - ConcurrentHashMap.put()이 이전 값(first)을 반환
        assertThat(result).isSameAs(first);
    }

    @Test
    void 같은_ID로_재저장하면_최신_알림으로_덮어쓴다() {
        // given
        LikeNotification first = 좋아요_알림("noti-1", 10L, 2L);
        LikeNotification second = 좋아요_알림("noti-1", 20L, 3L);
        sut.save(first);
        sut.save(second);

        // when
        Optional<Notification> result = sut.findById("noti-1");

        // then
        assertThat(result).isPresent().contains(second);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_빈_Optional을_반환한다() {
        // when
        Optional<Notification> result = sut.findById("not-exist");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 저장소가_비어있을_때_findById는_빈_Optional을_반환한다() {
        assertThat(sut.findById("any-id")).isEmpty();
    }

    // --- deleteById ---

    @Test
    void 알림을_삭제하면_이후_findById로_조회되지_않는다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 2L));

        // when
        sut.deleteById("noti-1");

        // then
        assertThat(sut.findById("noti-1")).isEmpty();
    }

    @Test
    void 존재하지_않는_ID를_삭제해도_예외가_발생하지_않는다() {
        // when & then - 정상 실행되어야 함
        sut.deleteById("not-exist");
    }

    @Test
    void 특정_알림만_삭제하면_다른_알림은_그대로_남는다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 2L));
        sut.save(좋아요_알림("noti-2", 20L, 3L));

        // when
        sut.deleteById("noti-1");

        // then
        assertThat(sut.findById("noti-1")).isEmpty();
        assertThat(sut.findById("noti-2")).isPresent();
    }

    // --- findByComment ---

    @Test
    void commentId로_댓글_알림을_조회할_수_있다() {
        // given
        CommentNotification notification = 댓글_알림("noti-1", 100L);
        sut.save(notification);

        // when
        Optional<Notification> result = sut.findByComment(100L);

        // then
        assertThat(result).isPresent().contains(notification);
    }

    @Test
    void 존재하지_않는_commentId로_조회하면_빈_Optional을_반환한다() {
        // given
        sut.save(댓글_알림("noti-1", 100L));

        // when
        Optional<Notification> result = sut.findByComment(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void LIKE_타입만_저장된_경우_commentId로_조회하면_빈_Optional을_반환한다() {
        // given - COMMENT가 아닌 LIKE만 저장
        sut.save(좋아요_알림("noti-1", 10L, 2L));

        // when
        Optional<Notification> result = sut.findByComment(100L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 저장소가_비어있을_때_findByComment는_빈_Optional을_반환한다() {
        assertThat(sut.findByComment(100L)).isEmpty();
    }

    @Test
    void 여러_댓글_알림_중_일치하는_commentId만_반환한다() {
        // given
        sut.save(댓글_알림("noti-1", 100L));
        sut.save(댓글_알림("noti-2", 200L));
        sut.save(댓글_알림("noti-3", 300L));

        // when
        Optional<Notification> result = sut.findByComment(200L);

        // then
        assertThat(result).isPresent();
        CommentNotification found = (CommentNotification) result.get();
        assertThat(found.getCommentId()).isEqualTo(200L);
    }

    // --- findLikeByPostIdAndLikedBy ---

    @Test
    void postId와_likedBy가_모두_일치하는_좋아요_알림을_조회할_수_있다() {
        // given
        LikeNotification notification = 좋아요_알림("noti-1", 10L, 2L);
        sut.save(notification);

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 2L);

        // then
        assertThat(result).isPresent().contains(notification);
    }

    @Test
    void postId만_다르면_좋아요_알림이_조회되지_않는다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 2L));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(99L, 2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void likedBy만_다르면_좋아요_알림이_조회되지_않는다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 2L));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 99L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void postId와_likedBy가_모두_다르면_좋아요_알림이_조회되지_않는다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 2L));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(99L, 99L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void COMMENT_타입만_저장된_경우_좋아요_알림_조회시_빈_Optional을_반환한다() {
        // given
        sut.save(댓글_알림("noti-1", 100L));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 2L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 저장소가_비어있을_때_findLikeByPostIdAndLikedBy는_빈_Optional을_반환한다() {
        assertThat(sut.findLikeByPostIdAndLikedBy(10L, 2L)).isEmpty();
    }

    @Test
    void 같은_postId에_다른_likedBy가_있을_때_정확한_likedBy만_반환한다() {
        // given
        sut.save(좋아요_알림("noti-1", 10L, 1L));
        sut.save(좋아요_알림("noti-2", 10L, 2L));
        sut.save(좋아요_알림("noti-3", 10L, 3L));

        // when
        Optional<Notification> result = sut.findLikeByPostIdAndLikedBy(10L, 2L);

        // then
        assertThat(result).isPresent();
        LikeNotification found = (LikeNotification) result.get();
        assertThat(found.getLikedIdsBy()).contains(2L);
    }

    // --- addLikeAtomically ---

    @Test
    void addLikeAtomically_문서가_없으면_새_좋아요_알림을_생성한다() {
        // when
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);

        // then
        Optional<Notification> result = sut.findLikeByPostId(10L);
        assertThat(result).isPresent();
        LikeNotification like = (LikeNotification) result.get();
        assertThat(like.getPostId()).isEqualTo(10L);
        assertThat(like.getUserId()).isEqualTo(1L);
        assertThat(like.getLikedIdsBy()).containsExactly(2L);
    }

    @Test
    void addLikeAtomically_문서가_있으면_likedIdsBy에_userId를_추가한다() {
        // given
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);

        // when
        sut.addLikeAtomically(10L, 1L, 3L, OCCURRED_AT);

        // then
        LikeNotification like = (LikeNotification) sut.findLikeByPostId(10L).orElseThrow();
        assertThat(like.getLikedIdsBy()).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void addLikeAtomically_이미_존재하는_userId는_중복_추가하지_않는다() {
        // given
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);

        // when
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);

        // then
        LikeNotification like = (LikeNotification) sut.findLikeByPostId(10L).orElseThrow();
        assertThat(like.getLikedIdsBy()).containsExactly(2L);
        assertThat(like.getLikedIdsBy()).hasSize(1);
    }

    @Test
    void addLikeAtomically_occurredAt이_최신으로_갱신된다() {
        // given
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);
        LocalDateTime laterTime = OCCURRED_AT.plusHours(1);

        // when
        sut.addLikeAtomically(10L, 1L, 3L, laterTime);

        // then
        LikeNotification like = (LikeNotification) sut.findLikeByPostId(10L).orElseThrow();
        assertThat(like.getOccurredAt()).isEqualTo(laterTime);
        assertThat(like.getExpiresAt()).isEqualTo(laterTime.plusDays(90));
    }

    // --- removeLikeAtomically ---

    @Test
    void removeLikeAtomically_likedIdsBy에서_userId를_제거한다() {
        // given
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);
        sut.addLikeAtomically(10L, 1L, 3L, OCCURRED_AT);

        // when
        sut.removeLikeAtomically(10L, 2L);

        // then
        LikeNotification like = (LikeNotification) sut.findLikeByPostId(10L).orElseThrow();
        assertThat(like.getLikedIdsBy()).containsExactly(3L);
    }

    @Test
    void removeLikeAtomically_마지막_userId_제거시_문서가_삭제된다() {
        // given
        sut.addLikeAtomically(10L, 1L, 2L, OCCURRED_AT);

        // when
        sut.removeLikeAtomically(10L, 2L);

        // then
        assertThat(sut.findLikeByPostId(10L)).isEmpty();
    }

    @Test
    void removeLikeAtomically_문서가_없으면_아무_동작도_하지_않는다() {
        // when & then — 예외 없이 정상 실행
        sut.removeLikeAtomically(99L, 2L);
    }

    // --- 픽스처 ---

    private LikeNotification 좋아요_알림(String notificationId, Long postId, Long likedBy) {
        return new LikeNotification(
                notificationId,
                1L,
                OCCURRED_AT,
                LocalDateTime.now(),
                OCCURRED_AT.plusDays(90),
                postId,
                List.of(likedBy));
    }

    private CommentNotification 댓글_알림(String notificationId, Long commentId) {
        return new CommentNotification(
                notificationId,
                1L,
                OCCURRED_AT,
                LocalDateTime.now(),
                OCCURRED_AT.plusDays(90),
                10L,
                2L,
                commentId,
                "테스트 댓글");
    }
}
