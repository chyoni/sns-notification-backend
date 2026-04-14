package cwchoiit.notification.core.infrastructure.mongo;

import cwchoiit.notification.core.infrastructure.mongo.persistence.CommentNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.FollowNotificationMongoEntity;
import cwchoiit.notification.core.infrastructure.mongo.persistence.LikeNotificationMongoEntity;
import java.util.Collections;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "cwchoiit.notification",
        mongoTemplateRef = MongoTemplateConfig.MONGO_TEMPLATE)
public class MongoTemplateConfig {

    public static final String MONGO_TEMPLATE = "notificationMongoTemplate";

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());
        MongoMappingContext context = new MongoMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.setInitialEntitySet(
                Set.of(
                        CommentNotificationMongoEntity.class,
                        LikeNotificationMongoEntity.class,
                        FollowNotificationMongoEntity.class));
        return context;
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext mongoMappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter converter =
                new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setTypeMapper(
                new DefaultMongoTypeMapper(
                        DefaultMongoTypeMapper.DEFAULT_TYPE_KEY, mongoMappingContext));
        return converter;
    }

    @Bean
    public MongoTemplate notificationMongoTemplate(
            MongoDatabaseFactory mongoDatabaseFactory,
            MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
    }
}
