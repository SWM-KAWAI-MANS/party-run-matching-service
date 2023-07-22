package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.global.sse.MultiSinkHandler;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WaitingEventService")
@SpringBootTest(classes = {WaitingEventService.class, MultiSinkHandler.class})
class WaitingEventServiceTest {
    @Autowired WaitingEventService waitingEventService;
    @Autowired ServerSentEventHandler<String, WaitingStatus> sseHandler;

    Mono<String> user1 = Mono.just("현준");
    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Waiting에_대한_이벤트_구독_시 {
        @Test
        @DisplayName("Connection 이벤트를발행한다")
        void publishConnection() {
            sseHandler.create(user1.block());

            StepVerifier.create(waitingEventService.getEventStream(user1))
                    .expectNext(WaitingStatus.CONNECTED)
                    .thenCancel()
                    .verify();
        }
    }

    @Test
    @DisplayName("member 명단을 받으면 match Event를 전송한다")
    void runSendMatchEvent() {
        final List<String> members = List.of("현준", "준혁", "성우");
        members.forEach(waitingEventService::register);

        waitingEventService.sendMatchEvent(members);
        members.forEach(member -> {
            StepVerifier.create(sseHandler.connect(member))
                    .expectNext(WaitingStatus.MATCHED)
                    .thenCancel().verify();
        });
    }
}