package online.partyrun.partyrunmatchingservice;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(RedisTestConfig.class)
class PartyRunMatchingServiceApplicationTests {

    @Test
    void contextLoads() {}
}
