package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.WaitingQueue;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("WaitingEventService")
@SpringBootTest
@Import(RedisTestConfig.class)
class WaitingEventServiceTest {
    @Autowired WaitingEventService waitingEventService;
    @Autowired WaitingService waitingService;
    @Autowired WaitingSinkHandler waitingSinkHandler;
    @Autowired WaitingQueue waitingQueue;

    Mono<String> user1 = Mono.just("현준");

    @BeforeEach
    void before() {
        waitingSinkHandler.shutdown();
        waitingQueue.clear();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 여러명이_등록되었을_때 {
        final List<String> members = List.of("현준", "준혁", "성우");

        @Test
        @DisplayName("event에 MATCHED가 포함되면 match Event를 전송하고 sink를 종료한다")
        void runDisconnect() {
            members.forEach(waitingEventService::register);
            waitingEventService.sendMatchEvent(members);
            members.forEach(
                    member ->
                            StepVerifier.create(
                                            waitingEventService.getEventStream(Mono.just(member)))
                                    .expectNext(
                                            new WaitingEventResponse(WaitingStatus.CONNECTED),
                                            new WaitingEventResponse(WaitingStatus.MATCHED))
                                    .verifyComplete());
        }
    }

    @Test
    @DisplayName("timeout된 sink를 삭제한다")
    void runDeleteSink() {

        waitingSinkHandler.create("현준");
        waitingEventService.removeUnConnectedSink();

        assertThat(waitingSinkHandler.getConnectors()).doesNotContain("현준");
    }

    @Test
    @DisplayName("shutdown을 진행한다")
    void runShutdown() {
        final List<String> members = List.of("현준", "준혁");
        members.forEach(waitingEventService::register);
        waitingEventService.shutdown();

        assertThat(waitingSinkHandler.getConnectors()).isEmpty();
    }

    @Test
    @DisplayName("매칭 중 취소하면 sink를 삭제하고, 대기queue에도 삭제한다")
    void runCancel() {
        waitingService.create(user1, new CreateWaitingRequest(1000)).block();

        waitingEventService.cancel(user1).block();
        final WaitingEventResponse response = waitingEventService.getEventStream(user1).blockLast();
        assertAll(
                () -> assertThat(waitingQueue.hasMember(user1.block())).isFalse(),
                () -> assertThat(response.status()).isEqualTo(WaitingStatus.CANCEL.toString()),
                () -> assertThat(waitingSinkHandler.getConnectors()).isNotIn(user1.block()));
    }
}
