package online.partyrun.application.domain.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.domain.waiting.repository.WaitingRepository;
import online.partyrun.application.global.handler.ServerSentEventHandler;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

@SpringBootTest
@DisplayName("WaitingService")
class WaitingServiceTest {
    @Autowired WaitingService waitingService;

    @Autowired ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;
    @Autowired WaitingRepository waitingRepository;

    Mono<String> runner = Mono.just("userID");
    RunningDistance distance = RunningDistance.M10000;
    CreateWaitingRequest request = new CreateWaitingRequest(distance);

    @AfterEach
    public void clear() {
        Arrays.stream(RunningDistance.values())
                .forEach(rd -> waitingRepository.findTop(rd, waitingRepository.size(rd)));
    }

    @Test
    @DisplayName("등록을 수행한다")
    void runRegister() {
        waitingService.register(runner, request);

        assertAll(
                () -> assertThat(waitingEventHandler.connect(runner.block())).isNotNull(),
                () -> assertThat(waitingRepository.size(distance)).isEqualTo(1));
    }

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
        @DisplayName("queue에서 추출을 수행한다")
        void runPollQueue() {
            waitingService.register(runner, request);
            waitingService.register(runner2, request);
            waitingService.register(runner3, request);

            assertThat(waitingRepository.size(distance)).isZero();
        }

        @Test
        @DisplayName("각 맴버들에게 완료 이벤트를 전송하고 커넥션을 종료한다.")
        void sendCompleteEventAndCloseConnection() {
            waitingService.register(runner, request);
            waitingService.register(runner2, request);
            waitingService.register(runner3, request);

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
