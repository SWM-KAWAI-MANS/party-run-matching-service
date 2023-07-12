package online.partyrun.partyrunmatchingservice.global.db.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("online.partyrun.application")
@EnableReactiveMongoAuditing
public class ReactiveMongoConfig {}
