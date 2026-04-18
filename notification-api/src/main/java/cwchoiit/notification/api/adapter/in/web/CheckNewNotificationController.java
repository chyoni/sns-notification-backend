package cwchoiit.notification.api.adapter.in.web;

import cwchoiit.notification.api.application.port.in.CheckNewNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class CheckNewNotificationController implements CheckNewNotificationControllerSpec {
    private final CheckNewNotificationUseCase checkNewNotificationUseCase;

    @Override
    @GetMapping("/{userId}/new")
    public ResponseEntity<Boolean> checkNew(@PathVariable Long userId) {
        return ResponseEntity.ok(checkNewNotificationUseCase.checkNewNotification(userId));
    }
}
