package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("waiting 프로세스 테스트")
@SpringBootTest
@Import(RedisTestConfig.class)
class WaitingProcessTest {
    @Autowired
    CreateWaitingService createWaitingService;
    @Autowired WaitingEventService waitingEventService;

    @Autowired WaitingSinkHandler waitingSinkHandler;

    @Autowired
    WaitingQueue waitingQueue;
    @Autowired
    MatchingRepository matchingRepository;

    Mono<String> 현준 = Mono.just("현준");
    Mono<String> 성우 = Mono.just("성우");

    CreateWaitingRequest request = new CreateWaitingRequest(1000);

    @Test
    @DisplayName("waiting 생성이 일정 회수가 되면, 각 사용자에게 sink를 제공하고 구독한다")
    void runProcess() {
        createWaitingService.create(현준, request).block();
        createWaitingService.create(성우, request).block();

        StepVerifier.create(waitingSinkHandler.connect(현준.block()))
                .expectNext(WaitingStatus.CONNECTED, WaitingStatus.MATCHED)
                .thenCancel()
                .verify();

        StepVerifier.create((waitingSinkHandler.connect(성우.block())))
                .expectNext(WaitingStatus.CONNECTED, WaitingStatus.MATCHED)
                .thenCancel()
                .verify();
    }


    @Test
    @DisplayName("동시에 요청해도 단 1회의 matching만 생성한다.")
    void runParallel() {
        Mono<String> aa = Mono.just("aa");
        Mono<String> bb = Mono.just("bb");

        Mono.zip(
                createWaitingService.create(현준, request),
                createWaitingService.create(성우, request),
                createWaitingService.create(aa, request),
                createWaitingService.create(bb, request)
        ).publishOn(Schedulers.parallel()).then()
                .publishOn(Schedulers.boundedElastic())
                        .doOnSuccess(s ->  assertThat(matchingRepository.findAll().count().block()).isEqualTo(2))
                                .block();

        assertThat(waitingQueue.hasMember(현준.block()).block()).isFalse();
        assertThat(waitingQueue.hasMember(성우.block()).block()).isFalse();
        assertThat(waitingQueue.hasMember(aa.block()).block()).isFalse();
        assertThat(waitingQueue.hasMember(bb.block()).block()).isFalse();
    }
}
