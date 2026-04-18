package cwchoiit.notification.api.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 알림센터 API")
public interface NotificationReadControllerSpec {

    @Operation(summary = "사용자가 알림 목록을 읽은 시간 기록")
    ResponseEntity<LocalDateTime> recordLastReadAt(@Parameter(example = "1") Long userId);
}
