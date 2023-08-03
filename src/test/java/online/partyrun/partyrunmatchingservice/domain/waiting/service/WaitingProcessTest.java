package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("waiting 프로세스 테스트")
@SpringBootTest
@Import(RedisTestConfig.class)
class WaitingProcessTest {
    @Autowired WaitingService waitingService;
    @Autowired WaitingEventService waitingEventService;

    @Autowired ServerSentEventHandler<String, WaitingStatus> sseHandler;

    Mono<String> 현준 = Mono.just("현준");
    Mono<String> 성우 = Mono.just("성우");

    CreateWaitingRequest request = new CreateWaitingRequest(1000);

    @Test
    @DisplayName("waiting 생성이 일정 회수가 되면, 각 사용자에게 sink를 제공하고 구독한다")
    void runProcess() {
        waitingService.create(현준, request).block();
        waitingService.create(성우, request).block();

        StepVerifier.create(sseHandler.connect(현준.block()))
                .expectNext(WaitingStatus.CONNECTED, WaitingStatus.MATCHED)
                .thenCancel()
                .verify();

        StepVerifier.create((sseHandler.connect(성우.block())))
                .expectNext(WaitingStatus.CONNECTED, WaitingStatus.MATCHED)

                .thenCancel()
                .verify();
    }
}
