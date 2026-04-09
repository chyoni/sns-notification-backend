package cwchoiit.notification.core.adapter.out.persistence;

import cwchoiit.notification.core.infrastructure.mongo.persistence.NotificationMongoEntity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotificationMongoRepository
        extends MongoRepository<NotificationMongoEntity, String> {

    @Query("{ 'commentId': ?0 }")
    Optional<NotificationMongoEntity> findByCommentId(Long commentId);
}
