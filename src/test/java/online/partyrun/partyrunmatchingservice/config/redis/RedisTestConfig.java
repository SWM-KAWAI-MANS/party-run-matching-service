package online.partyrun.partyrunmatchingservice.config.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class RedisTestConfig {
    private static final int REDIS_PORT = 6379;

    @Container
    private static GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.0.8-alpine"))
            .withExposedPorts(REDIS_PORT).withReuse(true);

    static {
        REDIS.start();
        System.setProperty("spring.data.redis.host", REDIS.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(REDIS.getMappedPort(REDIS_PORT)));
    }

    @BeforeEach
    void setup() {
        if(!REDIS.isCreated()) {
            REDIS.start();

        }
    }

    @AfterEach
    void finish() {
        REDIS.stop();
    }
}
