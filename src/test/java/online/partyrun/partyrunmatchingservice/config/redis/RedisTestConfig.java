package online.partyrun.partyrunmatchingservice.config.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import redis.embedded.RedisServer;

@TestConfiguration
public class RedisTestConfig {

    @Value("${spring.data.redis.port:#{6379}}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() {
        redisServer = new RedisServer(redisPort);
        try {
            redisServer.start();

        } catch (Exception e) {
        }
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:" + redisPort);
        return Redisson.create(config);
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }
}
