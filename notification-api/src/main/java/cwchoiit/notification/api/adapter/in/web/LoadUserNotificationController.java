package cwchoiit.notification.api.adapter.in.web;

import cwchoiit.notification.api.application.model.ConvertNotification;
import cwchoiit.notification.api.application.port.in.LoadUserNotificationUseCase;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class LoadUserNotificationController implements LoadUserNotificationControllerSpec {
    private final LoadUserNotificationUseCase loadUserNotificationUseCase;

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<Slice<ConvertNotification>> loadNotifications(
            @PathVariable Long userId,
            @RequestParam(value = "pivot", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime pivot) {
        return ResponseEntity.ok(
                loadUserNotificationUseCase.getUserNotificationsByPivot(userId, pivot));
    }
}
