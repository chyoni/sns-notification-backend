package cwchoiit.notification.core.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class NotificationMongoRepositoryAdapterTest {
    @Container static MongoDBContainer mongoDb = new MongoDBContainer("mongo:7.0");

    static {
        mongoDb.start();
        System.setProperty("notification.mongo.uri", mongoDb.getReplicaSetUrl("notification"));
    }

    @Autowired private NotificationMongoRepositoryAdapter adapter;

    @Test
    void save() {
        Notification savedNotification =
                adapter.save(
                        new CommentNotification(
                                "1",
                                2L,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now().plusDays(90),
                                2L,
                                3L,
                                4L,
                                "Good Post!"));
        assertThat(adapter.findById("1").isPresent()).isTrue();
        assertThat(savedNotification.getNotificationId()).isEqualTo("1");
    }

    @Test
    void findById() {
        adapter.save(
                new CommentNotification(
                        "1",
                        2L,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        2L,
                        3L,
                        4L,
                        "Good Post!"));
        Notification notification = adapter.findById("1").orElseThrow();
        assertThat(notification.getNotificationId()).isEqualTo("1");
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.COMMENT);
        assertThat(notification.getUserId()).isEqualTo(2L);
    }

    @Test
    void deleteById() {
        adapter.save(
                new CommentNotification(
                        "1",
                        2L,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        2L,
                        3L,
                        4L,
                        "Good Post!"));

        adapter.deleteById("1");
        assertThat(adapter.findById("1").isPresent()).isFalse();
    }

    // ============================================================
    // addLikeAtomically
    // ============================================================

    @Test
    void addLikeAtomically_첫_좋아요시_도큐먼트가_생성된다() {
        // when
        adapter.addLikeAtomically(100L, 1L, 2L, LocalDateTime.now());

        // then
        Notification result = adapter.findLikeByPostId(100L).orElseThrow();
        LikeNotification like = (LikeNotification) result;
        assertThat(like.getPostId()).isEqualTo(100L);
        assertThat(like.getUserId()).isEqualTo(1L);
        assertThat(like.getLikedIdsBy()).containsExactly(2L);
    }

    @Test
    void addLikeAtomically_두번째_좋아요시_likedIdsBy에_추가된다() {
        // given
        adapter.addLikeAtomically(101L, 1L, 2L, LocalDateTime.now());

        // when
        adapter.addLikeAtomically(101L, 1L, 3L, LocalDateTime.now());

        // then
        LikeNotification like = (LikeNotification) adapter.findLikeByPostId(101L).orElseThrow();
        assertThat(like.getLikedIdsBy()).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void addLikeAtomically_중복_userId는_추가되지_않는다() {
        // given
        adapter.addLikeAtomically(102L, 1L, 2L, LocalDateTime.now());

        // when
        adapter.addLikeAtomically(102L, 1L, 2L, LocalDateTime.now());

        // then
        LikeNotification like = (LikeNotification) adapter.findLikeByPostId(102L).orElseThrow();
        assertThat(like.getLikedIdsBy()).hasSize(1).containsExactly(2L);
    }

    @Test
    void addLikeAtomically_동시_호출시_모든_userId가_반영된다() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        LocalDateTime now = LocalDateTime.now();

        // when — 서로 다른 userId 10명이 동시에 같은 게시물에 좋아요
        for (long userId = 1; userId <= threadCount; userId++) {
            final long uid = userId;
            executor.submit(
                    () -> {
                        try {
                            adapter.addLikeAtomically(200L, 99L, uid, now);
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        latch.await();
        executor.shutdown();

        // then — 10명 모두 likedIdsBy에 반영되어야 함
        LikeNotification like = (LikeNotification) adapter.findLikeByPostId(200L).orElseThrow();
        assertThat(like.getLikedIdsBy()).hasSize(threadCount);
        for (long userId = 1; userId <= threadCount; userId++) {
            assertThat(like.getLikedIdsBy()).contains(userId);
        }
    }

    @Test
    void addLikeAtomically_동시_첫_좋아요시_도큐먼트가_하나만_생성된다() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        LocalDateTime now = LocalDateTime.now();

        // when — 5명이 동시에 같은 게시물에 첫 좋아요 시도
        for (long userId = 1; userId <= threadCount; userId++) {
            final long uid = userId;
            executor.submit(
                    () -> {
                        try {
                            adapter.addLikeAtomically(201L, 99L, uid, now);
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        latch.await();
        executor.shutdown();

        // then — 도큐먼트는 정확히 1개, 5명 모두 반영
        LikeNotification like = (LikeNotification) adapter.findLikeByPostId(201L).orElseThrow();
        assertThat(like.getLikedIdsBy()).hasSize(threadCount);
    }

    // ============================================================
    // removeLikeAtomically
    // ============================================================

    @Test
    void removeLikeAtomically_userId를_제거한다() {
        // given
        adapter.addLikeAtomically(300L, 1L, 2L, LocalDateTime.now());
        adapter.addLikeAtomically(300L, 1L, 3L, LocalDateTime.now());

        // when
        adapter.removeLikeAtomically(300L, 2L);

        // then
        LikeNotification like = (LikeNotification) adapter.findLikeByPostId(300L).orElseThrow();
        assertThat(like.getLikedIdsBy()).containsExactly(3L);
    }

    @Test
    void removeLikeAtomically_마지막_userId_제거시_도큐먼트가_삭제된다() {
        // given
        adapter.addLikeAtomically(301L, 1L, 2L, LocalDateTime.now());

        // when
        adapter.removeLikeAtomically(301L, 2L);

        // then
        assertThat(adapter.findLikeByPostId(301L)).isEmpty();
    }
}
