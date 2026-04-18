package cwchoiit.notification.api.adapter.in.web;

import cwchoiit.notification.api.application.port.in.NotificationLastReadUseCase;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationReadController implements NotificationReadControllerSpec {

    private final NotificationLastReadUseCase notificationLastReadUseCase;

    @Override
    @PutMapping("/{userId}/read")
    public ResponseEntity<LocalDateTime> recordLastReadAt(@PathVariable Long userId) {
        LocalDateTime lastReadAt = notificationLastReadUseCase.recordLastReadAt(userId);
        return ResponseEntity.ok(lastReadAt);
    }
}
