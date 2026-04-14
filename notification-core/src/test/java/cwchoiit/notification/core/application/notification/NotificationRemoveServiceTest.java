package cwchoiit.notification.core.application.notification;

import static org.mockito.BDDMockito.then;

import cwchoiit.notification.core.application.port.out.NotificationRepository;
import cwchoiit.notification.core.domain.notification.CommentNotification;
import cwchoiit.notification.core.domain.notification.LikeNotification;
import cwchoiit.notification.core.domain.notification.Notification;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRemoveServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationRemoveService sut;

    @Test
    void 좋아요_알림_삭제시_해당_notificationId로_deleteById가_호출된다() {
        // given
        String notificationId = "like-noti-1";
        Notification notification = 좋아요_알림(notificationId);

        // when
        sut.removeNotification(notification);

        // then
        then(notificationRepository).should().deleteById(notificationId);
    }

    @Test
    void 댓글_알림_삭제시_해당_notificationId로_deleteById가_호출된다() {
        // given
        String notificationId = "comment-noti-99";
        Notification notification = 댓글_알림(notificationId);

        // when
        sut.removeNotification(notification);

        // then
        then(notificationRepository).should().deleteById(notificationId);
    }

    @Test
    void 알림_삭제시_deleteById_외에_다른_Repository_메서드는_호출되지_않는다() {
        // given
        Notification notification = 좋아요_알림("noti-1");

        // when
        sut.removeNotification(notification);

        // then
        then(notificationRepository).should().deleteById(notification.getNotificationId());
        then(notificationRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 서로_다른_알림을_순서대로_삭제하면_각각의_notificationId로_호출된다() {
        // given
        Notification first = 좋아요_알림("first-id");
        Notification second = 댓글_알림("second-id");

        // when
        sut.removeNotification(first);
        sut.removeNotification(second);

        // then
        then(notificationRepository).should().deleteById("first-id");
        then(notificationRepository).should().deleteById("second-id");
    }

    // --- 픽스처 ---

    private LikeNotification 좋아요_알림(String notificationId) {
        return new LikeNotification(
                notificationId,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(90),
                10L,
                2L);
    }

    private CommentNotification 댓글_알림(String notificationId) {
        return new CommentNotification(
                notificationId,
                1L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(90),
                10L,
                2L,
                100L,
                "댓글");
    }
}
