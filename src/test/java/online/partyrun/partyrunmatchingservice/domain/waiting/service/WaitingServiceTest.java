package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.WaitingEvent;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.DuplicateUserException;
import online.partyrun.partyrunmatchingservice.domain.waiting.repository.SubscribeBuffer;
import online.partyrun.partyrunmatchingservice.global.handler.ServerSentEventHandler;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@DisplayName("WaitingService")
@Import(RedisTestConfig.class)
class WaitingServiceTest {
    @Autowired WaitingService waitingService;

    @Autowired ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;
    @Autowired SubscribeBuffer buffer;

    Mono<String> runner = Mono.just("userID");
    RunningDistance distance = RunningDistance.M10000;
    CreateWaitingRequest request = new CreateWaitingRequest(distance);

    @AfterEach
    void cleanup() {
        waitingEventHandler.shutdown();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Runner_가_2명이_등록되면 {
        Mono<String> runner2 = Mono.just("성우");

        WaitingEventResponse connected = new WaitingEventResponse(WaitingEvent.CONNECT);
        WaitingEventResponse matched = new WaitingEventResponse(WaitingEvent.MATCHED);

        @Test
        @DisplayName("각 맴버들에게 완료 이벤트를 전송하고 커넥션을 종료한다.")
        void sendCompleteEventAndCloseConnection() {
            waitingService.register(runner, request).block();
            waitingService.register(runner2, request).block();

            StepVerifier.create(waitingService.subscribe(runner))
                    .assertNext(res -> assertThat(res).isEqualTo(connected))
                    .assertNext(res -> assertThat(res).isEqualTo(matched))
                    .expectNoAccessibleContext()
                    .verifyComplete();

            StepVerifier.create(waitingService.subscribe(runner2))
                    .assertNext(res -> assertThat(res).isEqualTo(connected))
                    .assertNext(res -> assertThat(res).isEqualTo(matched))
                    .expectNoAccessibleContext()
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("일정 시간마다 buffer에서 flush 되었지만, sse 커넥션을 안한 sink를 삭제한다")
    void runDeleteSink() {

        waitingEventHandler.addSink("현준");
        waitingService.removeUnConnectedSink();

        assertThat(waitingEventHandler.getConnectors()).doesNotContain("현준");
    }
}
