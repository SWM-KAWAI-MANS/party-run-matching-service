package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("WaitingEventService")
@SpringBootTest
@Import(RedisTestConfig.class)
class WaitingEventServiceTest {
    @Autowired
    WaitingEventService waitingEventService;
    @Autowired
    CreateWaitingService createWaitingService;
    @Autowired
    WaitingSinkHandler waitingSinkHandler;
    @Autowired
    WaitingQueue waitingQueue;
    @Autowired
    ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    MatchingRepository matchingRepository;


    Mono<String> user1 = Mono.just("현준");

    @AfterEach
    void after() {
        waitingSinkHandler.shutdown();
        waitingQueue.clear().block();
        waitingQueue.delete(user1.block()).block();
        matchingRepository.deleteAll().block();
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
        final String id1 = "삭제예정1";
        final String id2 = "삭제예정2";
        waitingSinkHandler.create(id1);
        waitingSinkHandler.create(id2);

        waitingEventService.runSchedule();

        assertThat(waitingSinkHandler.getConnectors()).doesNotContain(id1, id2);
    }

    @Test
    @DisplayName("shutdown을 진행한다")
    void runShutdown() {
        final List<String> members = List.of("현준", "준혁");
        members.forEach(waitingEventService::register);
        waitingEventService.shutdown().block();

        assertThat(waitingSinkHandler.getConnectors()).isEmpty();
    }

    @Test
    @DisplayName("취소 시에 cancel 이벤트를 전송한다")
    void runCancel() {
        createWaitingService.create(user1, new CreateWaitingRequest(1000)).block();

        waitingEventService.cancel(user1).block();

        final String waitingStatus = waitingEventService.getEventStream(user1).blockLast().status();
        assertAll(
                () -> assertThat(waitingQueue.hasMember(user1.block()).block()).isFalse(),
                () -> assertThat(waitingStatus).isEqualTo(WaitingStatus.CANCEL.name()),
                () -> assertThat(waitingSinkHandler.isExist(user1.block())).isFalse()
        );

    }
}
