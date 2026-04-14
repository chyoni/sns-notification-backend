package cwchoiit.notification.core.infrastructure.mongo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    public MongoIndexInitializer(
            @Qualifier(MongoTemplateConfig.MONGO_TEMPLATE) MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        mongoTemplate
                .indexOps("notification")
                .createIndex(
                        new Index()
                                .on("postId", Sort.Direction.ASC)
                                .unique()
                                .partial(
                                        PartialIndexFilter.of(
                                                Criteria.where("notificationType").is("LIKE")))
                                .named("uidx_postId_like"));
    }
}
