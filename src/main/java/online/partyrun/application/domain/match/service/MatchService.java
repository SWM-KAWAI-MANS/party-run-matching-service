package online.partyrun.application.domain.match.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.domain.MatchEvent;
import online.partyrun.application.domain.match.domain.Runner;
import online.partyrun.application.domain.match.domain.RunnerStatus;
import online.partyrun.application.domain.match.dto.CreateBattleRequest;
import online.partyrun.application.domain.match.dto.MatchEventResponse;
import online.partyrun.application.domain.match.dto.MatchRequest;
import online.partyrun.application.domain.match.repository.MatchRepository;
import online.partyrun.application.domain.match.repository.RunnerRepository;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.global.handler.ServerSentEventHandler;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Function;

/**
 * match 관련 service를 제공합니다. {@link ServerSentEventHandler}를 이용하여 SSE 통신 register 및 subscribe을 지원합니다.
 * 외부 Battle API를 호출하여 Battle 생성을 요청합니다.
 *
 * @author Hyeonjun Park
 * @see ServerSentEventHandler
 * @see MatchRepository
 * @see RunnerRepository
 * @see WebClient
 * @since 2023-06-29
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    ServerSentEventHandler<String, MatchEvent> matchEventHandler;
    WebClient battleClient = WebClient.create("http://localhost:8080"); // TODO : battle service와 연결
    MatchRepository matchRepository;
    RunnerRepository runnerRepository;

    /**
     * runner의 참여 여부를 설정합니다. join 여부에 따라 status를 변경하고, 변경 상황을 announce 합니다.
     *
     * @param runner 유저 ID
     * @param request 참여 여부를 설정할 정보
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    public void setParticipation(final Mono<String> runner, final MatchRequest request) {
        runner.subscribe(
                runnerId -> {
                    log.info("setParticipation 실행 {}", runnerId);
                    if (request.isJoin()) {
                        changeStatus(runnerId, RunnerStatus.REDDY);
                        announceMatching(runnerId);
                        return;
                    }
                    changeStatus(runnerId, RunnerStatus.CANCELLED);
                    announceCancel(runnerId);
                });
    }

    /**
     * Runner의 status를 변경합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void changeStatus(final String runnerId, final RunnerStatus status) {
        log.info("changeStatus 실행 {}", runnerId);
        getRunner(runnerId)
                .doOnNext(runner -> runner.changeStatus(status))
                .subscribe(runner -> runnerRepository.save(runner).subscribe());
    }

    /**
     * runner가 포함된 match의 STAND_BY 조건이 성립이 되면 standBy 상태로 변경합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void announceMatching(final String runnerId) {
        announce(runnerId, this::isMatchSuccess, MatchEvent.COMPLETE);
    }

    /**
     * runner가 포함된 match의 CANCEL 조건이 성립이 되면 cansel 상태로 변경합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void announceCancel(final String runnerId) {
        announce(runnerId, this::isCancelled, MatchEvent.CANCEL);
    }

    /**
     * runner가 포함된 match 대상자들에게 어떠한 상태가 만족하면 event를 발행합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void announce(
            final String runnerId,
            final Function<String, Mono<Boolean>> checkStatus,
            final MatchEvent event) {
        getMatchIdByRunnerId(runnerId)
                .subscribe(matchId -> checkMatchStatus(checkStatus, event, matchId));
    }

    /**
     * runner가 포함된 match의 Id를 반환합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Mono<String> getMatchIdByRunnerId(final String runnerId) {
        return getRunner(runnerId).map(Runner::getMatchId);
    }

    /**
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Mono<Runner> getRunner(final String runnerId) {
        return runnerRepository.findByMemberId(runnerId);
    }

    /**
     * checkStatus가 만족하면 match에 포함된 runner들에게 event를 발행한 후 완료시킵니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void checkMatchStatus(
            final Function<String, Mono<Boolean>> checkStatus,
            final MatchEvent event,
            final String matchId) {
        checkStatus
                .apply(matchId)
                .subscribe(
                        checkResult -> {
                            if (Boolean.TRUE.equals(checkResult)) {
                                announceAndComplete(event, matchId);
                            }
                        });
    }

    /**
     * match에 해당하는 runner들에게 event를 전달하고 완료시킵니다. 완료시킨 후에 repository에서 match와 runner들을 제거합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void announceAndComplete(final MatchEvent event, final String matchId) {
        getRunnersByMatchId(matchId)
                .map(Runner::getMemberId)
                .subscribe(
                        runnerId -> {
                            matchEventHandler.sendEvent(runnerId, event);
                            runnerRepository.deleteById(runnerId);
                        });
        matchRepository.deleteById(matchId);
    }

    /**
     * match의 Cancel 여부를 판단합니다. runners 중에 한 명이라도 cancel 한다면 true를 반환합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Mono<Boolean> isCancelled(final String matchId) {
        return getAllRunnerStatus(matchId).hasElement(RunnerStatus.CANCELLED);
    }

    /**
     * match의 StandBy 여부를 판단합니다. 모든 runner가 Reddy 상태가 되면 true를 반환합니다. 동시에, Battle 생성을 요청합니다
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Mono<Boolean> isMatchSuccess(final String matchId) {
        return getAllRunnerStatus(matchId)
                .all(status -> status == RunnerStatus.REDDY)
                .doOnNext(
                        result -> {
                            if (Boolean.TRUE.equals(result)) {
                                createBattle(matchId);
                            }
                        });
    }

    /**
     * Battle 생성을 요청합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void createBattle(final String matchId) {
        matchRepository
                .findById(matchId)
                .map(Match::getDistance)
                .subscribe(
                        distance -> {
                            getRunnersByMatchId(matchId)
                                    .map(Runner::getMemberId)
                                    .collectList()
                                    .subscribe(runners -> requestCreateBattle(runners, distance));
                        });
    }

    /**
     * matchId에 해당하는 모든 runner들을 가져옵니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Flux<Runner> getRunnersByMatchId(final String matchId) {
        return runnerRepository.findAllByMatchId(matchId);
    }

    /**
     * matchId에 해당하는 runner들의 status를 가져옵니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private Flux<RunnerStatus> getAllRunnerStatus(final String matchId) {
        return getRunnersByMatchId(matchId).map(Runner::getStatus);
    }

    /**
     * 외부 API를 호출해 Battle 생성을 요청합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void requestCreateBattle(final List<String> runners, int distance) {
        battleClient
                .post()
                .uri("/api/battle")
                .bodyValue(new CreateBattleRequest(runners, distance))
                .retrieve();
    }

    /**
     * Match를 생성합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    public Mono<Match> createMatch(final List<String> runnerIds, RunningDistance distance) {
        return matchRepository
                .save(new Match(distance.getMeter()))
                .doOnNext(match -> createRunners(runnerIds, match));
    }

    /**
     * Runner들을 생성합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    private void createRunners(final List<String> runners, final Match match) {
        runners.forEach(
                rid ->
                        runnerRepository
                                .save(new Runner(rid, match.getId(), RunnerStatus.NO_RESPONSE))
                                .doOnNext(runner -> matchEventHandler.addSink(runner.getMemberId()))
                                .subscribe());
    }

    /**
     * Matching Event에 대한 구독 진행합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    public Flux<MatchEventResponse> subscribe(Mono<String> runner) {
        return runner.map(
                        id ->
                                matchEventHandler
                                        .connect(id)
                                        .doOnNext(
                                                event -> {
                                                    if (!event.equals(MatchEvent.CONNECT)) {
                                                        matchEventHandler.complete(id);
                                                    }
                                                })
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(MatchEventResponse::new))
                .flatMapMany(f -> f);
    }
}
