package online.partyrun.application;

import online.partyrun.application.config.redis.RedisTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RedisTestConfig.class)
class ApplicationTests {

    @Test
    void contextLoads() {}
}
