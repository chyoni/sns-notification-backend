package cwchoiit.notification.core.infrastructure.mongo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.mongo")
public record MongoProperties(String uri) {}
