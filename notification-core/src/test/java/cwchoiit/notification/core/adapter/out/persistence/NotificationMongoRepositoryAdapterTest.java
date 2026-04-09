package cwchoiit.notification.core.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
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
}
