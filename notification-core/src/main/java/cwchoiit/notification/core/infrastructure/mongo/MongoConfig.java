package cwchoiit.notification.core.infrastructure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MongoProperties.class)
public class MongoConfig {
    private final MongoProperties mongoProperties;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoProperties.uri());
        MongoClientSettings settings =
                MongoClientSettings.builder().applyConnectionString(connectionString).build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, getDatabaseFromUri());
    }

    private String getDatabaseFromUri() {
        return new ConnectionString(mongoProperties.uri()).getDatabase();
    }
}
