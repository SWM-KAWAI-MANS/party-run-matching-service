package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis.WaitingQueue;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveListOperations;
import reactor.core.publisher.Flux;
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
    @Autowired
    WaitingEventService waitingEventService;

    @Autowired
    WaitingSinkHandler waitingSinkHandler;

    @Autowired
    WaitingQueue waitingQueue;
    @Autowired
    MatchingRepository matchingRepository;
    @Autowired
    ReactiveListOperations<RunningDistance, String> waitingListOperations;

    Mono<String> 현준 = Mono.just("현준");
    Mono<String> 성우 = Mono.just("성우");
    Mono<String> 현식 = Mono.just("현식");
    Mono<String> 준혁 = Mono.just("준혁");
    Mono<String> 세연 = Mono.just("세연");

    CreateWaitingRequest request = new CreateWaitingRequest(1000);
    @AfterEach
    void afterEach() {
        waitingListOperations.delete(RunningDistance.M1000).block();
        waitingSinkHandler.shutdown();
        matchingRepository.deleteAll().block();
    }
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
    @DisplayName("순차적으로 동작했을 때 매칭을 생성하고, 큐에 삭제한다")
    void runCreate() {
        Flux.concat(
                        createWaitingService.create(현준, request),
                        createWaitingService.create(성우, request),
                        createWaitingService.create(현식, request),
                        createWaitingService.create(준혁, request),
                        createWaitingService.create(세연, request)
                ).publishOn(Schedulers.single()).then()
                .block();

        final Mono<Long> count = waitingListOperations.range(RunningDistance.M1000, 0, -1).count();
        assertThat(matchingRepository.findAll().count().block()).isEqualTo(2);
        StepVerifier.create(count).expectNext(1L).verifyComplete();
    }


    @Test
    @DisplayName("동시에 요청해도 단 중복 없이 매칭을 생성하고, 큐에 삭제한다.")
    void runParallel() {
        Flux.concat(
                        createWaitingService.create(현준, request),
                        createWaitingService.create(성우, request),
                        createWaitingService.create(현식, request),
                        createWaitingService.create(준혁, request),
                        createWaitingService.create(세연, request)
                ).publishOn(Schedulers.parallel()).then()
                .block();

        final Mono<Long> count = waitingListOperations.range(RunningDistance.M1000, 0, -1).count();
        assertThat(matchingRepository.findAll().count().block()).isEqualTo(2);
        StepVerifier.create(count).expectNext(1L).verifyComplete();
    }
}
