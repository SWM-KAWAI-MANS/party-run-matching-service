package online.partyrun.application.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.application.domain.match.service.MatchService;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.domain.WaitingRunner;
import online.partyrun.application.domain.waiting.dto.CreateWaitingRequest;
import online.partyrun.application.domain.waiting.dto.WaitingEventResponse;
import online.partyrun.application.domain.waiting.mapper.WaitingRunnerMapper;
import online.partyrun.application.domain.waiting.repository.WaitingRepository;
import online.partyrun.application.global.handler.ServerSentEventHandler;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * waiting 관련 service를 제공합니다. {@link ServerSentEventHandler}를 이용하여 SSE 통신 register 및 subscribe을
 * 지원합니다. 또한, 대기열이 조건에 만족하면 match로 연계하도록 구현하였습니다. 현재는 대기열에 3명을 정원으로 설정하였습니다.
 *
 * @author Hyeonjun Park
 * @see ServerSentEventHandler
 * @see MatchService
 * @see WaitingRepository
 * @see WaitingRunnerMapper
 * @since 2023-06-29
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingService {
    static int MATCHING_COUNT = 3;

    ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;
    MatchService matchService;
    WaitingRepository waitingRepository;
    WaitingRunnerMapper waitingRunnerMapper;

    /**
     * 새로운 대기열을 등록합니다. eventHandler에 sink를 등록한 후, 대기열 queue에 저장합니다.
     *
     * @param runner 사용자 id가 담겨있는 {@link Mono}
     * @param request waiting 생성시에 필요한 요구사항
     * @author Hyeonjun Park
     * @since 2023-06-29
     */
    public void register(Mono<String> runner, CreateWaitingRequest request) {
        runner.subscribe(
                id -> {
                    waitingEventHandler.addSink(id);
                    addQueue(waitingRunnerMapper.toEntity(id, request));
                });
    }

    /**
     * 대기열을 구독합니다. sink에 연결한 후, 들어오는 event를 사용자에게 바로 전달합니다.
     *
     * @param runner 사용자 id가 담겨있는 {@link Mono}
     * @return event stream
     * @author Hyeonjun Park
     * @since 2023-06-29
     */
    public Flux<WaitingEventResponse> subscribe(Mono<String> runner) {
        return runner.map(
                        id ->
                                waitingEventHandler
                                        .connect(id)
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(WaitingEventResponse::new))
                .flatMapMany(f -> f);
    }

    /**
     * Queue에 새롭게 저장합니다. queue에 정해진 값 이상이 되면 match 생성 로직을 수행합니다. match 생성 로직을 수행한 이후, 연결중인 사용자 sink를
     * complete 합니다.
     *
     * @param waitingRunner 새롭게 추가하는 runner
     * @author Hyeonjun Park
     * @see MatchService
     * @see WaitEventHandler
     * @since 2023-06-29
     */
    private void addQueue(final WaitingRunner waitingRunner) {
        waitingRepository.save(waitingRunner);
        final RunningDistance distance = waitingRunner.distance();
        while (waitingRepository.size(distance) >= MATCHING_COUNT) {
            final List<String> runners = waitingRepository.findTop(distance, MATCHING_COUNT);
            matchService.createMatch(runners, distance).subscribe();
            runners.forEach(
                    id -> {
                        waitingEventHandler.addEvent(id, WaitingEvent.MATCHED);
                        waitingEventHandler.complete(id);
                    });
        }
    }
}
