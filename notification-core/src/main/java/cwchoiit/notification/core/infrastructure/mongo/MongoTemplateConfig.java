package cwchoiit.notification.core.infrastructure.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "cwchoiit.notification",
        mongoTemplateRef = MongoTemplateConfig.MONGO_TEMPLATE)
public class MongoTemplateConfig {

    public static final String MONGO_TEMPLATE = "notificationMongoTemplate";

    @Bean
    public MongoTemplate notificationMongoTemplate(
            MongoDatabaseFactory mongoDatabaseFactory, MongoConverter mongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
    }
}
