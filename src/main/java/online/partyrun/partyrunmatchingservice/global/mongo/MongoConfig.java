package online.partyrun.partyrunmatchingservice.global.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("online.partyrun.partyrunmatchingservice")
@EnableReactiveMongoAuditing
public class MongoConfig {}
