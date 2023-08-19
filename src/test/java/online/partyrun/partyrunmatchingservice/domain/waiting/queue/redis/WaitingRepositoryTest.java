package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfig.class)
@DisplayName("waitingRepository 에서")
class WaitingRepositoryTest {
    @Autowired
    WaitingRepository waitingRepository;
    @Test
    @DisplayName("저장 및 조회를 수행한다")
    void runSave() {
        final String id = "asdf";
        final int distance = 1000;
        Waiting waiting = new Waiting(id, distance);

        waitingRepository.save(waiting);

        final Waiting waiting1 = waitingRepository.findById(id).orElseThrow();
        assertThat(waiting1.getDistance()).isEqualTo(distance);
        assertThat(waiting1.getId()).isEqualTo(id);
    }
}