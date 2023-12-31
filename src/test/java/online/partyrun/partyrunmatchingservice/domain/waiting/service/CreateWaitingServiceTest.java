package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveListOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("WaitingService")
@Import(RedisTestConfig.class)
class CreateWaitingServiceTest {
    @Autowired
    CreateWaitingService createWaitingService;
    @Autowired WaitingSinkHandler sseHandler;
    @Autowired
    MatchingRepository matchingRepository;
    @Autowired
    ReactiveListOperations<RunningDistance, String> waitingListOperations;

    @AfterEach
    void afterEach() {
        waitingListOperations.delete(RunningDistance.M1000).block();
        sseHandler.shutdown();
        matchingRepository.deleteAll().block();
    }

    Mono<String> user1 = Mono.just("현준");

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting생성_시 {
        @Test
        @DisplayName("Message를 반환한다")
        void returnMessage() {
            StepVerifier.create(createWaitingService.create(user1, new CreateWaitingRequest(1000)))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Sink를 생성한다.")
        void createSseSink() {
            createWaitingService.create(user1, new CreateWaitingRequest(1000)).block();

            assertThat(sseHandler.getConnectors()).contains(user1.block());
        }
    }
}
