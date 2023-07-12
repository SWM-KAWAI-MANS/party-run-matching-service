package online.partyrun.partyrunmatchingservice.domain.match.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchMember;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchStatus;
import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.match.dto.MatchRequest;
import online.partyrun.partyrunmatchingservice.domain.match.repository.MatchRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.domain.RunningDistance;
import online.partyrun.partyrunmatchingservice.global.handler.ServerSentEventHandler;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@DisplayName("matchService")
@Import(RedisTestConfig.class)
class MatchServiceTest {
    @Autowired
    MatchService matchService;
    @Autowired MatchRepository matchRepository;
    @Autowired ServerSentEventHandler<String, MatchEvent> matchEventHandler;
    @MockBean Clock clock;

    List<String> memberIds = List.of("현준", "성우", "준혁");

    @AfterEach
    void cleanup() {
        matchEventHandler.shutdown();
        matchRepository.deleteAll().block();
    }

    @Test
    @DisplayName("생성을 수행한다")
    void runCreate() {
        StepVerifier.create(matchService.create(memberIds, RunningDistance.M1000))
                .assertNext(
                        match -> {
                            assertThat(match.getMembers().stream().map(MatchMember::getId))
                                    .contains("현준", "성우", "준혁");
                            assertThat(match.getDistance())
                                    .isEqualTo(RunningDistance.M1000.getMeter());
                            assertThat(match.getStatus()).isEqualTo(MatchStatus.WAIT);
                        })
                .verifyComplete();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Member생성을_진행한_후 {
        Mono<String> 현준 = Mono.just(memberIds.get(0));
        Mono<String> 성우 = Mono.just(memberIds.get(1));
        Mono<String> 준혁 = Mono.just(memberIds.get(2));
        MatchRequest 수락 = new MatchRequest(true);
        MatchRequest 거절 = new MatchRequest(false);

        @Test
        @DisplayName("member 상태를 설정한다")
        void runSetMemberStatus() {
            matchService.create(memberIds, RunningDistance.M1000).block();

            StepVerifier.create(matchService.setMemberStatus(현준, 수락))
                    .assertNext(
                            match -> {
                                assertThat(match.getMembers().stream().map(MatchMember::getId))
                                        .contains("현준", "성우", "준혁");
                                assertThat(match.getDistance())
                                        .isEqualTo(RunningDistance.M1000.getMeter());
                                assertThat(match.getStatus()).isEqualTo(MatchStatus.WAIT);
                            })
                    .verifyComplete();
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class member가_거절할_경우 {
            @Test
            @DisplayName("매치 상태를 캔슬로 변경한다")
            void runSetMemberStatus() {
                matchService.create(memberIds, RunningDistance.M1000).block();

                StepVerifier.create(matchService.setMemberStatus(현준, 거절))
                        .assertNext(
                                match -> {
                                    assertThat(match.getMembers().stream().map(MatchMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCEL);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("이전에 사람들이 수락을 했어도 캔슬로 변경한다")
            void runCancel() {
                matchService.create(memberIds, RunningDistance.M1000).block();
                matchService.setMemberStatus(성우, 수락).block();
                matchService.setMemberStatus(준혁, 수락).block();

                StepVerifier.create(matchService.setMemberStatus(현준, 거절))
                        .assertNext(
                                match -> {
                                    assertThat(match.getMembers().stream().map(MatchMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCEL);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("내가 수락을 했어도 다른사람이 거절하면 캔슬로 변경한다")
            void runCancelIf() {
                matchService.create(memberIds, RunningDistance.M1000).block();
                matchService.setMemberStatus(성우, 수락).block();
                matchService.setMemberStatus(준혁, 거절).block();

                StepVerifier.create(matchService.setMemberStatus(현준, 수락))
                        .assertNext(
                                match -> {
                                    assertThat(match.getMembers().stream().map(MatchMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchStatus.CANCEL);
                                })
                        .verifyComplete();
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class member모두가_수락한_경우 {
            @Test
            @DisplayName("member 상태를 설정한 후에 매치 상태도 성공으로 변경한다")
            void runSetMemberStatus() {
                matchService.create(memberIds, RunningDistance.M1000).block();
                matchService.setMemberStatus(성우, 수락).block();
                matchService.setMemberStatus(준혁, 수락).block();
                StepVerifier.create(matchService.setMemberStatus(현준, 수락))
                        .assertNext(
                                match -> {
                                    assertThat(match.getMembers().stream().map(MatchMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchStatus.SUCCESS);
                                })
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("구독을 진행한다")
        void runSubscribe() {
            matchService.create(memberIds, RunningDistance.M1000).block();
            matchService.setMemberStatus(현준, 거절).block();

            StepVerifier.create(matchService.subscribe(현준))
                    .expectNextCount(2)
                    .expectNoAccessibleContext()
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("생성시에 기존에 Wait중인 데이터가 있으면 기존 데이터를 삭제한다")
    void runDeleteIfExistMatch() {
        matchService.create(memberIds, RunningDistance.M1000).block();
        matchService.create(memberIds, RunningDistance.M1000).block();

        StepVerifier.create(matchRepository.findAllByStatus(MatchStatus.WAIT))
                .expectNextCount(1)
                .expectAccessibleContext();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Scadule이_동작할때 {

        @BeforeEach
        void setUp() {
            matchService.create(memberIds, RunningDistance.M1000).block();
            given(clock.getZone()).willReturn(ZoneOffset.ofHours(9));
        }

        @Test
        @DisplayName("WAIT 된지 2시간이 지난 Match를 삭제한다")
        void runDeleteIfTimeOver() {
            setdelay(3);
            matchService.removeUnConnectedSink();

            StepVerifier.create(matchRepository.findAllByStatus(MatchStatus.WAIT))
                    .expectNextCount(0)
                    .expectAccessibleContext();
            assertThat(matchEventHandler.getConnectors()).isEmpty();
        }

        @Test
        @DisplayName("WAIT 된지 2시간이 지나지 않으면 Match를 삭제하지 않는다")
        void runNotDeleteIfNotTimeOver() {
            setdelay(1);
            matchService.removeUnConnectedSink();

            StepVerifier.create(matchRepository.findAllByStatus(MatchStatus.WAIT))
                    .expectNextCount(1)
                    .expectAccessibleContext();
            assertThat(matchEventHandler.getConnectors()).isNotEmpty();
        }

        private BDDMockito.BDDMyOngoingStubbing<Instant> setdelay(final int hours) {
            return given(clock.instant())
                    .willReturn(
                            LocalDateTime.now().plusHours(hours).toInstant(ZoneOffset.ofHours(9)));
        }
    }
}
