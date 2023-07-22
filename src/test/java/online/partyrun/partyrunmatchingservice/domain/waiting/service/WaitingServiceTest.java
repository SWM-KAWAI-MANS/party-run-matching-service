package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@DisplayName("WaitingService")
class WaitingServiceTest {
    @Autowired WaitingService waitingService;
    @Autowired ServerSentEventHandler<String, WaitingStatus> sseHandler;

    Mono<String> user1 = Mono.just("현준");

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting생성_시 {
        @Test
        @DisplayName("Message를 반환한다")
        void returnMessage() {
            StepVerifier.create(waitingService.create(user1, new CreateWaitingRequest(1000)))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Sink를 생성한다.")
        void createSseSink() {
            waitingService.create(user1, new CreateWaitingRequest(1000)).block();

            assertThat(sseHandler.getConnectors()).contains(user1.block());
        }

        @Test
        @DisplayName("REGISTERED 이벤트를 발행한다.")
        void publishRegistered() {
            waitingService.create(user1, new CreateWaitingRequest(1000)).block();

            StepVerifier.create(sseHandler.connect(user1.block()))
                    .assertNext(res -> assertThat(res).isEqualTo(WaitingStatus.REGISTERED))
                    .thenCancel()
                    .verify();
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting에_대한_이벤트_구독_시 {
        @Test
        @DisplayName("Connection 이벤트를발행한다")
        void publishConnection() {
            sseHandler.create(user1.block());

            StepVerifier.create(waitingService.getEventStream(user1))
                    .assertNext(res -> assertThat(res).isEqualTo(WaitingStatus.CONNECTED))
                    .thenCancel()
                    .verify();
        }
    }
}
