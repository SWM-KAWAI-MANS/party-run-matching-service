package online.partyrun.partyrunmatchingservice.config.redis;

import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class RedisTestConfig {

    private static RedisServer redisServer = new RedisServer(6379);
    static {
        redisServer.start();
    }
}
