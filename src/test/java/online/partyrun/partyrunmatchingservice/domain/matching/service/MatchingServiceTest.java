package online.partyrun.partyrunmatchingservice.domain.matching.service;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.matching.controller.MatchingRequest;
import online.partyrun.partyrunmatchingservice.domain.matching.dto.MatchEvent;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;
import online.partyrun.partyrunmatchingservice.domain.matching.repository.MatchingRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@DisplayName("MatchingService")
@Import(RedisTestConfig.class)
class MatchingServiceTest {
    @Autowired MatchingService matchingService;

    @Autowired ServerSentEventHandler<String, MatchEvent> sseHandler;
    @Autowired MatchingRepository matchingRepository;

    final List<String> members = List.of("현준", "성우", "준혁");
    Mono<String> 현준 = Mono.just(members.get(0));
    Mono<String> 성우 = Mono.just(members.get(1));
    Mono<String> 준혁 = Mono.just(members.get(2));
    MatchingRequest 수락 = new MatchingRequest(true);
    MatchingRequest 거절 = new MatchingRequest(false);
    final int distance = 1000;

    @BeforeEach
    void cleanup() {
        sseHandler.shutdown();
        matchingRepository.deleteAll().block();
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
        matchingRepository.updateMatchingMemberStatus(
                matchingResult.getId(), members.get(0), MatchingMemberStatus.CANCELED);

        matchingService.create(members, distance).block();

        assertThat(sseHandler.getConnectors().stream().filter(m -> m.equals(members.get(0))))
                .hasSize(1);
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class Member생성을_진행한_후_Member_상태_변경_요청시 {

        @Test
        @DisplayName("member 상태를 설정한다")
        void runSetMemberStatus() {
            matchingService.create(members, distance).block();

            StepVerifier.create(matchingService.setMemberStatus(현준, 수락))
                    .assertNext(
                            match -> {
                                assertThat(match.getMembers().stream().map(MatchingMember::getId))
                                        .contains("현준", "성우", "준혁");
                                assertThat(match.getDistance()).isEqualTo(distance);
                                assertThat(match.getStatus()).isEqualTo(MatchingStatus.WAIT);
                            })
                    .verifyComplete();
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class member가_거절할_경우 {
            @Test
            @DisplayName("매치 상태를 캔슬로 변경한다")
            void runSetMemberStatus() {
                matchingService.create(members, distance).block();

                StepVerifier.create(matchingService.setMemberStatus(현준, 거절))
                        .assertNext(
                                match -> {
                                    assertThat(
                                                    match.getMembers().stream()
                                                            .map(MatchingMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchingStatus.CANCEL);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("이전에 사람들이 수락을 했어도 캔슬로 변경한다")
            void runCancel() {
                matchingService.create(members, distance).block();
                matchingService.setMemberStatus(성우, 수락).block();
                matchingService.setMemberStatus(준혁, 수락).block();

                StepVerifier.create(matchingService.setMemberStatus(현준, 거절))
                        .assertNext(
                                match -> {
                                    assertThat(
                                                    match.getMembers().stream()
                                                            .map(MatchingMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchingStatus.CANCEL);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("내가 수락을 했어도 다른사람이 거절하면 캔슬로 변경한다")
            void runCancelIf() {
                matchingService.create(members, distance).block();
                matchingService.setMemberStatus(성우, 수락).block();
                matchingService.setMemberStatus(준혁, 거절).block();

                StepVerifier.create(matchingService.setMemberStatus(현준, 수락))
                        .assertNext(
                                match -> {
                                    assertThat(
                                                    match.getMembers().stream()
                                                            .map(MatchingMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchingStatus.CANCEL);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("동시 요청시에도 수행한다.")
            @SneakyThrows
            void runParallel() {
                final Matching matcing = matchingService.create(members, distance).block();

                final Mono<Matching> publisher1 = matchingService.setMemberStatus(현준, 수락);
                final Mono<Matching> publisher2 = matchingService.setMemberStatus(성우, 수락);
                final Mono<Matching> publisher3 = matchingService.setMemberStatus(준혁, 수락);
                Flux.zip(publisher1, publisher2, publisher3)
                        .subscribeOn(Schedulers.parallel())
                        .blockLast();

                final Matching block = matchingRepository.findById(matcing.getId()).block();
                assertThat(block.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
            }

            @Test
            @DisplayName("구독을 진행시에 거절 이벤트를 받고 종료한다.")
            void runSubscribe() {
                matchingService.create(members, 1000).block();
                matchingService.setMemberStatus(현준, 거절).block();

                StepVerifier.create(matchingService.getEventSteam(현준))
                        .expectNextCount(2)
                        .verifyComplete();
                assertThat(sseHandler.getConnectors()).isNotIn(현준.block());
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class member모두가_수락한_경우 {
            @Test
            @DisplayName("member 상태를 설정한 후에 매치 상태도 성공으로 변경한다")
            void runSetMemberStatus() {
                matchingService.create(members, distance).block();
                matchingService.setMemberStatus(성우, 수락).block();
                matchingService.setMemberStatus(준혁, 수락).block();
                StepVerifier.create(matchingService.setMemberStatus(현준, 수락))
                        .assertNext(
                                match -> {
                                    assertThat(
                                                    match.getMembers().stream()
                                                            .map(MatchingMember::getId))
                                            .contains("현준", "성우", "준혁");
                                    assertThat(match.getDistance())
                                            .isEqualTo(RunningDistance.M1000.getMeter());
                                    assertThat(match.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
                                })
                        .verifyComplete();
            }

            @Test
            @DisplayName("구독을 진행시에 수락 이벤트를 받고 종료한다.")
            void runSubscribe() {
                matchingService.create(members, 1000).block();
                int eventCount = 1 + members.size(); // Connection 값, 각 member 수락 이벤트 값 포함
                members.forEach(
                        member -> matchingService.setMemberStatus(Mono.just(member), 수락).block());

                members.forEach(
                        member ->
                                StepVerifier.create(
                                                matchingService.getEventSteam(Mono.just(member)))
                                        .expectNextCount(eventCount)
                                        .verifyComplete());
                assertThat(sseHandler.getConnectors()).isEmpty();
            }
        }
    }
}
