package cwchoiit.notification.api.adapter.in.web;

import cwchoiit.notification.api.application.model.ConvertNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Notification API")
public interface LoadUserNotificationControllerSpec {

    @Operation(summary = "Load User Notification list API")
    ResponseEntity<Slice<ConvertNotification>> loadNotifications(
            @Parameter(example = "1") Long userId,
            @Parameter(example = "2026-04-11T00:00:00") LocalDateTime pivot);
}
