package online.partyrun.partyrunmatchingservice.config.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import redis.embedded.RedisServer;

@TestConfiguration
public class RedisTestConfig {

    @Value("${spring.data.redis.port:#{6379}}")
    private int redisPort;

    private static RedisServer redisServer;

    @PostConstruct
    public void redisServer() {
        redisServer = new RedisServer(redisPort);
        try {
            redisServer.start();

        } catch (Exception e) {
        }
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }
}
