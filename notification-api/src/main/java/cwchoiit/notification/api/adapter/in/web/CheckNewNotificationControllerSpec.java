package cwchoiit.notification.api.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Notification API")
public interface CheckNewNotificationControllerSpec {
    @Operation(summary = "사용자 신규 알림 여부 조회")
    ResponseEntity<Boolean> checkNew(@Parameter(example = "11") Long userId);
}
