package cwchoiit.notification.core.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import cwchoiit.notification.core.domain.notification.NotificationType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class NotificationInMemoryRepositoryAdapterTest {

    private final NotificationRepository sut = new NotificationInMemoryRepositoryAdapter();

    @Test
    void save() {
        sut.save(
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
        assertThat(sut.findById("1").isPresent()).isTrue();
    }

    @Test
    void findById() {
        sut.save(
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
        Notification notification = sut.findById("1").orElseThrow();
        assertThat(notification.getNotificationId()).isEqualTo("1");
        assertThat(notification.getNotificationType()).isEqualTo(NotificationType.COMMENT);
        assertThat(notification.getUserId()).isEqualTo(2L);
    }

    @Test
    void deleteById() {
        sut.save(
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

        sut.deleteById("1");
        assertThat(sut.findById("1").isPresent()).isFalse();
    }
}
