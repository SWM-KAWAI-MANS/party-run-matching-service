package online.partyrun.partyrunmatchingservice.config;

import online.partyrun.partyrunmatchingservice.config.mongodb.MongodbTestConfig;
import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RedisTestConfig.class, MongodbTestConfig.class})
@SpringBootTest
public @interface IntegrationTest {
}
