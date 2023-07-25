package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@DisplayName("WaitingEventService")
@SpringBootTest(classes = {WaitingEventService.class, WaitingSinkHandler.class})
class WaitingEventServiceTest {
    @Autowired WaitingEventService waitingEventService;
    @Autowired WaitingSinkHandler waitingSinkHandler;

    Mono<String> user1 = Mono.just("현준");

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting에_대한_이벤트_구독_시 {
        @Test
        @DisplayName("Connection 이벤트를발행한다")
        void publishConnection() {
            waitingSinkHandler.create(user1.block());

            StepVerifier.create(waitingEventService.getEventStream(user1))
                    .expectNext(WaitingStatus.CONNECTED)
                    .thenCancel()
                    .verify();
        }
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
                                    .expectNext(WaitingStatus.MATCHED, WaitingStatus.CONNECTED)
                                    .verifyComplete());
        }
    }
}
