package online.partyrun.partyrunmatchingservice.config.mongodb;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MongodbTestConfig {
    private static final int MONGO_PORT = 27017;
    @Container
    private static final MongoDBContainer MONGODB = new MongoDBContainer(DockerImageName.parse("mongo:6"))
            .withExposedPorts(MONGO_PORT).withReuse(true);

    static {
        MONGODB.start();
        System.setProperty("spring.data.mongodb.host", MONGODB.getHost());
        System.setProperty("spring.data.mongodb.port", String.valueOf(MONGODB.getMappedPort(MONGO_PORT)));
    }

}
