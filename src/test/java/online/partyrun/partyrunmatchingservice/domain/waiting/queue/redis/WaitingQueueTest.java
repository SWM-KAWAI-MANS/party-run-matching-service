package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfig.class)
@DisplayName("WaitingQueue에서")
class WaitingQueueTest {

    @Autowired
    WaitingQueue waitingQueue;

    final String 현준 = "현준";
    final String 성우 = "성우";
    final int distance = 1000;

    @AfterEach
    void afterEach() {
        waitingQueue.clear().block();
    }

    @Test
    @DisplayName("hasMember를 수행한다")
    void runHasMember() {

        waitingQueue.add(new WaitingMember(현준, distance)).block();

        assertThat(waitingQueue.hasMember(현준).block()).isTrue();
        assertThat(waitingQueue.hasMember(성우).block()).isFalse();
    }

    @Test
    @DisplayName("추가 이후 clear를 수행한다")
    void runClear() {
        waitingQueue.add(new WaitingMember(현준, distance)).block();
        waitingQueue.add(new WaitingMember(성우, distance)).block();

        waitingQueue.clear().block();

        assertThat(waitingQueue.hasMember(현준).block()).isFalse();
        assertThat(waitingQueue.hasMember(성우).block()).isFalse();
    }

    @Test
    @DisplayName("추가 이후 삭제를 수행한다")
    void runDelete() {
        waitingQueue.add(new WaitingMember(현준, distance)).block();

        waitingQueue.delete(현준).block();

        assertThat(waitingQueue.hasMember(현준).block()).isFalse();
    }

    @Test
    @DisplayName("만족하는 수만큼 그룹하여 전송한다")
    void runFindNextGroup() {
        waitingQueue.add(new WaitingMember(현준, distance)).block();
        waitingQueue.add(new WaitingMember(성우, distance)).block();

        final List<String> nextGroup = waitingQueue.findNextGroup(RunningDistance.getBy(distance)).block();

        assertThat(nextGroup).hasSize(2);
    }

    @Test
    @DisplayName("수가 만족하지 않으면 Mono Empty를 반환한다")
    void runReturnEmptyList() {
        waitingQueue.add(new WaitingMember(현준, distance)).block();

        final Mono<List<String>> nextGroup = waitingQueue.findNextGroup(RunningDistance.getBy(distance));

        StepVerifier.create(nextGroup)
                .expectNextCount(0)
                .verifyComplete();
    }
}