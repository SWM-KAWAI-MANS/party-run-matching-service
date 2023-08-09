package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.exception.RunnerAlreadyRunningException;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@DisplayName("WaitingService")
@Import(RedisTestConfig.class)
class WaitingServiceTest {
    @Autowired
    WaitingService waitingService;
    @Autowired
    WaitingSinkHandler sseHandler;
    @MockBean
    BattleService battleService;


    Mono<String> user1 = Mono.just("현준");
    final CreateWaitingRequest request = new CreateWaitingRequest(1000);

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting생성_시 {
        @BeforeEach
        public void beforeEach() {
            given(battleService.isRunning(any(String.class))).willReturn(Mono.just(false));
        }

        @Test
        @DisplayName("Message를 반환한다")
        void returnMessage() {
            StepVerifier.create(waitingService.create(user1, request))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Sink를 생성한다.")
        void createSseSink() {
            waitingService.create(user1, request).block();

            assertThat(sseHandler.getConnectors()).contains(user1.block());
        }
    }

    @Test
    @DisplayName("이미 사용자가 배틀중이면 예외를 반환한다")
    void throwExceptionIfBattleRunning() {
        given(battleService.isRunning(user1.block())).willReturn(Mono.just(true));

        StepVerifier.create(waitingService.create(user1, request))
                .expectError(RunnerAlreadyRunningException.class)
                .verify();
    }
}
