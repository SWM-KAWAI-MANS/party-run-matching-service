package online.partyrun.application.domain.match.service;

import online.partyrun.application.config.redis.RedisTestConfig;
import online.partyrun.application.domain.match.domain.MatchEvent;
import online.partyrun.application.domain.match.domain.RunnerStatus;
import online.partyrun.application.domain.match.dto.MatchEventResponse;
import online.partyrun.application.domain.match.dto.MatchRequest;
import online.partyrun.application.domain.match.repository.MatchRepository;
import online.partyrun.application.domain.match.repository.RunnerRepository;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfig.class)
@DisplayName("MatchService")
class MatchServiceTest {

    @Autowired
    MatchService matchService;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    RunnerRepository runnerRepository;

    @Autowired
    MatchEventHandler matchEventHandler;
    Mono<String> runner = Mono.just("runnerID");

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 참가를_수락하면 {

        MatchRequest request = new MatchRequest(true);

        @Test
        @DisplayName("참여 로직을 수행한다")
        void runParticipationAccept() {
            matchService.setParticipation(runner, request);
        }
    }


    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 참가를_거절하면 {
        MatchRequest request = new MatchRequest(false);

        @Test
        @DisplayName("참여 거절 로직을 수행한다")
        void runParticipationReject() {
            matchService.setParticipation(runner, request);
        }
    }

    @Test
    @DisplayName("Match 생성을 수행한다")
    void runCreateMatch() {
        List<String> runners = List.of("runner1", "runner2", "runner3");


        StepVerifier.create(matchService.createMatch(runners, RunningDistance.M1000))
                .assertNext(res ->
                        assertThat(res.getDistance()).isEqualTo(RunningDistance.M1000.getMeter()))
                .verifyComplete();

        StepVerifier.create(runnerRepository.findByMemberId("runner1"))
                .assertNext(res -> {
                    assertThat(res.getMemberId()).isEqualTo("runner1");
                    assertThat(res.getStatus()).isEqualTo(RunnerStatus.NO_RESPONSE);
                }).verifyComplete();

        matchEventHandler.complete("runner1");

        StepVerifier.create(matchEventHandler.connect("runner1"))
                .expectNext(MatchEvent.CONNECT)
                .verifyComplete();
    }

    @Test
    @DisplayName("Match event 구독을 수행한다")
    void runSubscribe() {
        // final String runnerId = runner.block();
        List<String> runners = List.of("runnerID", "runner2", "runner3");

        MatchEventResponse expected = new MatchEventResponse(MatchEvent.CONNECT);
        matchService.createMatch(runners, RunningDistance.M1000).block();

        final Flux<MatchEventResponse> subscribe = matchService.subscribe(runner);


        // Not Run
        StepVerifier.create(subscribe)
                .assertNext(res -> assertThat(res).isEqualTo(expected))
                .as("Match event 구독을 수행한다");
    }
}