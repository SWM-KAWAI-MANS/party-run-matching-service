package online.partyrun.partyrunmatchingservice.domain.matching.service;

import lombok.SneakyThrows;
import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@DisplayName("MatchingService")
@Import(RedisTestConfig.class)
class MatchingServiceTest {
    @Autowired MatchingService matchingService;
    @Autowired MatchingSinkHandler sseHandler;
    @Autowired MatchingRepository matchingRepository;
    @Autowired Clock clock;
    @MockBean BattleService battleService;
    String 현준 = "현준";
    String 성우 = "성우";
    String 준혁 = "준혁";
    final List<String> members = List.of(현준, 성우, 준혁);
    MatchingRequest 수락 = new MatchingRequest(true);
    MatchingRequest 거절 = new MatchingRequest(false);
    final int distance = 1000;

    @AfterEach
    public void cleanup() {
        matchingRepository.deleteAll().block();
        sseHandler.shutdown();
    }

    @BeforeEach
    public void setupBattle() {
        given(battleService.create(any(List.class), any(Integer.class)))
                .willReturn(Mono.just("battleId"));
    }

    @Test
    @DisplayName("matching을 생성한다")
    void runCreate() {
        StepVerifier.create(matchingService.create(members, distance))
                .assertNext(
                        matching -> {
                            assertThat(matching.getDistance()).isEqualTo(1000);
                            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.WAIT);
                            assertThat(matching.getMembers().stream().map(MatchingMember::getId))
                                    .containsAll(members);
                        })
                .verifyComplete();
    }

    @Test
    @DisplayName("match 생성 시 sink connect를 생성한다")
    void runCreateSink() {
        matchingService.create(members, distance).block();
        assertThat(sseHandler.getConnectors()).containsAll(members);
    }

    @Test
    @DisplayName("match 생성 시 기존 sink가 남아있으면 완료한 후에 재연결한다.")
    void runDeleteSinkBeforeCreate() {
        final Matching matching = matchingService.create(members, distance).block();
        final Matching matchingResult = matchingRepository.save(matching).block();
        matchingRepository
                .updateMatchingMemberStatus(
                        matchingResult.getId(), members.get(0), MatchingMemberStatus.CANCELED)
                .block();

        matchingService.create(members, distance).block();

        assertThat(sseHandler.getConnectors().stream().filter(m -> m.equals(members.get(0))))
                .hasSize(1);
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class member_모두가_승인시 {
        Matching matching;

        @BeforeEach
        void setup() {
            matching = matchingService.create(members, distance).block();
            matchingService.setMemberStatus(Mono.just(현준), 수락).block();
            matchingService.setMemberStatus(Mono.just(성우), 수락).block();
            matchingService.setMemberStatus(Mono.just(준혁), 수락).block();
        }

        @Test
        @DisplayName("매치 상태가 success 로 반환한다")
        void changeSuccess() {
            StepVerifier.create(matchingRepository.findById(matching.getId()))
                    .assertNext(m -> assertThat(m.getStatus()).isEqualTo(MatchingStatus.SUCCESS))
                    .verifyComplete();
        }

        @Test
        @DisplayName("구독 진행 시 수락 이벤트를 받고 종료된다")
        void completeSubscribe() {
            final MatchEvent result = matchingService.getEventSteam(Mono.just(현준)).blockLast();
            assertThat(result.status()).isEqualTo(MatchingStatus.SUCCESS);
        }

        @Test
        @DisplayName("배틀 생성을 요청한 후에 배틀 id를 이벤트로 전송한다")
        void createBattle() {
            final Flux<String> getEvent =
                    matchingService
                            .getEventSteam(Mono.just(현준))
                            .filter(matchEvent -> Objects.nonNull(matchEvent.battleId()))
                            .map(MatchEvent::battleId);
            StepVerifier.create(getEvent).expectNext("battleId").verifyComplete();
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class 한_명이라도_거절_시 {
        Matching matching;

        @BeforeEach
        void setup() {
            matching = matchingService.create(members, distance).block();
            matchingService.setMemberStatus(Mono.just(현준), 수락).block();
            matchingService.setMemberStatus(Mono.just(성우), 수락).block();
            matchingService.setMemberStatus(Mono.just(준혁), 거절).block();
        }

        @Test
        @DisplayName("매치 상태가 cancel 로 반환한다")
        void changeSuccess() {
            StepVerifier.create(matchingRepository.findById(matching.getId()))
                    .assertNext(m -> assertThat(m.getStatus()).isEqualTo(MatchingStatus.CANCEL))
                    .verifyComplete();
        }

        @Test
        @DisplayName("구독 진행 시 cancel 이벤트를 받고 종료된다")
        void completeSubscribe() {
            final MatchEvent result = matchingService.getEventSteam(Mono.just(현준)).blockLast();
            assertThat(result.status()).isEqualTo(MatchingStatus.CANCEL);
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class 스케줄러가_동작할_때 {
        Clock mockClock = Clock.fixed(clock.instant().plusSeconds(14400), clock.getZone());

        MatchingService laterMatchingService =
                new MatchingService(matchingRepository, sseHandler, battleService, mockClock);

        @Test
        @DisplayName("TIMEOUT된 Match를 삭제한다")
        @SneakyThrows
        void runDeleteIfTimeOver() {
            matchingService.create(members, 1000).block();

            Mono.defer(
                            () -> {
                                laterMatchingService.removeUnConnectedSink();
                                return Mono.delay(Duration.ofMillis(20));
                            })
                    .publishOn(Schedulers.boundedElastic())
                    .doOnTerminate(
                            () -> {
                                final Matching matching =
                                        matchingRepository
                                                .findAllByMembersStatus(
                                                        MatchingMemberStatus.NO_RESPONSE)
                                                .blockLast();
                                assertThat(sseHandler.getConnectors()).isEmpty();
                                assertThat(matching).isNull();
                            })
                    .subscribe();
        }

        @Test
        @DisplayName("WAIT 된지 2시간이 지나지 않으면 Match를 삭제하지 않는다")
        void runNotDeleteIfNotTimeOver() {

            matchingService.create(members, 1000).block();
            matchingService.removeUnConnectedSink();

            StepVerifier.create(
                            matchingRepository.findAllByMembersStatus(
                                    MatchingMemberStatus.NO_RESPONSE))
                    .expectNextCount(1)
                    .verifyComplete();
            assertThat(sseHandler.getConnectors()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("id에 해당되는 matching을 탐색한다")
    void runFindById() {
        final Matching matching = matchingService.create(members, distance).block();

        StepVerifier.create(matchingService.getById(matching.getId()))
                .assertNext(m -> {
                    assertThat(m.id()).isEqualTo(matching.getId());
                    assertThat(m.distance()).isEqualTo(matching.getDistance());
                    assertThat(m.members()).hasSize(matching.getMembers().size());
                })
                .verifyComplete();
    }
}
