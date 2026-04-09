package cwchoiit.notification.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cwchoiit.notification")
public class NotificationConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationConsumerApplication.class, args);
    }
}
