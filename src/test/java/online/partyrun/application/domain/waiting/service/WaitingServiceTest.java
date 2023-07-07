package online.partyrun.application.domain.waiting.service;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.global.handler.ServerSentEventHandler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("WaitingService")
class WaitingServiceTest {
    @Autowired WaitingService waitingService;

    @Autowired ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;

    Mono<String> runner = Mono.just("userID");
    RunningDistance distance = RunningDistance.M10000;
    CreateWaitingRequest request = new CreateWaitingRequest(distance);

    @Test
    @DisplayName("구독을 수행한다")
    void runSubscribe() {
        waitingService.register(runner, request);
        final Flux<WaitingEventResponse> subscribe = waitingService.subscribe(runner);

        assertThat(subscribe).isNotNull();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Runner_가_3명이_등록되면 {
        Mono<String> runner2 = Mono.just("userID2");
        Mono<String> runner3 = Mono.just("userID3");

        WaitingEventResponse connected = new WaitingEventResponse(WaitingEvent.CONNECT);
        WaitingEventResponse matched = new WaitingEventResponse(WaitingEvent.MATCHED);

        @Test
        @DisplayName("각 맴버들에게 완료 이벤트를 전송하고 커넥션을 종료한다.")
        void sendCompleteEventAndCloseConnection() {
            waitingService.register(runner, request).block();
            waitingService.register(runner2, request).block();
            waitingService.register(runner3, request).block();

            StepVerifier.create(waitingService.subscribe(runner))
                    .assertNext(res -> assertThat(res).isEqualTo(connected))
                    .assertNext(res -> assertThat(res).isEqualTo(matched))
                    .verifyComplete();

            StepVerifier.create(waitingService.subscribe(runner2))
                    .assertNext(res -> assertThat(res).isEqualTo(connected))
                    .assertNext(res -> assertThat(res).isEqualTo(matched))
                    .verifyComplete();

            StepVerifier.create(waitingService.subscribe(runner3))
                    .assertNext(res -> assertThat(res).isEqualTo(connected))
                    .assertNext(res -> assertThat(res).isEqualTo(matched))
                    .verifyComplete();
        }
    }
}
