package online.partyrun.partyrunmatchingservice.domain.party.service;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.battle.service.BattleService;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyEvent;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyIdResponse;
import online.partyrun.partyrunmatchingservice.domain.party.dto.PartyRequest;
import online.partyrun.partyrunmatchingservice.domain.party.entity.EntryCode;
import online.partyrun.partyrunmatchingservice.domain.party.entity.Party;
import online.partyrun.partyrunmatchingservice.domain.party.entity.PartyStatus;
import online.partyrun.partyrunmatchingservice.domain.party.exception.PartyNotFoundException;
import online.partyrun.partyrunmatchingservice.domain.party.repository.PartyRepository;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@DisplayName("partyService")
@Import(RedisTestConfig.class)
class PartyServiceTest {
    @Autowired
    PartyService partyService;
    @Autowired
    PartyRepository partyRepository;
    @Autowired
    PartySinkHandler partySinkHandler;
    @MockBean
    BattleService battleService;


    Mono<String> 현준 = Mono.just("현준");
    Mono<String> 성우 = Mono.just("성우");
    Party partySample = new Party(현준.block(), RunningDistance.M1000);

    @AfterEach
    public void clear() {
        partyRepository.deleteAll().block();
        partySinkHandler.shutdown();
    }

    @Test
    @DisplayName("party 생성 후 EntryCode를 반환한다")
    void createParty() {
        final int distance = 1000;
        final PartyIdResponse response = partyService.create(현준, new PartyRequest(distance)).block();

        final Party findResult = partyRepository.findByEntryCodeAndStatus(new EntryCode(response.code()), PartyStatus.WAITING).block();

        assertAll(
                () -> assertThat(findResult.getDistance()).isEqualTo(RunningDistance.getBy(distance)),
                () -> assertThat(findResult.getManagerId()).isEqualTo(현준.block())
        );
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class 파티가_생성된_후 {
        EntryCode code = partyRepository.save(partySample).block().getEntryCode();
        PartyEvent event = new PartyEvent(code.getCode(), 1000, 현준.block(), PartyStatus.WAITING, Set.of(현준.block()), null);

        @BeforeEach
        void setUp() {
            given(battleService.create(any(List.class), any(Integer.class)))
                    .willReturn(Mono.just("battleId"));
            code = partyRepository.save(partySample).block().getEntryCode();
        }

        @Test
        @DisplayName("join 시에 party 명단에 추가하고, envet를 sink를 생성하여 참여자에게 전파한다.")
        void joinParty() {

            StepVerifier.create(partyService.joinAndConnectSink(현준, code.getCode()))
                    .assertNext(res -> assertThat(res).isEqualTo(event))
                    .thenCancel()
                    .verify();
        }

        @Test
        @DisplayName("게임 시작시에 이벤트를 전송하고, 완료 처리를 한다.")
        void gameStart() {

            partyService.joinAndConnectSink(현준, code.getCode()).blockFirst();
            partySinkHandler.create(현준.block());
            partyService.joinAndConnectSink(성우, code.getCode()).blockFirst();
            partySinkHandler.create(성우.block());
            partyService.start(현준, code.getCode()).block();

            final Party partyResult = partyRepository.findByEntryCodeAndStatus(code, PartyStatus.COMPLETED).block();
            assertAll(
                    () -> assertThat(partySinkHandler.isExist(현준.block())).isFalse(),
                    () -> assertThat(partySinkHandler.isExist(성우.block())).isFalse(),
                    () -> assertThat(partyResult.getParticipantIds()).contains(현준.block(), 성우.block())
            );
        }

        @Test
        @DisplayName("파티 나가기를 수행한다.")
        void quitParty() {
            partyService.joinAndConnectSink(현준, code.getCode()).blockFirst();
            partySinkHandler.create(현준.block());
            partyService.joinAndConnectSink(성우, code.getCode()).blockFirst();
            partySinkHandler.create(성우.block());

            partyService.quit(현준, code.getCode()).block();
            final Party partyResult = partyRepository.findByEntryCodeAndStatus(code, PartyStatus.CANCELLED).block();

            assertAll(
                    () -> assertThat(partySinkHandler.isExist(현준.block())).isFalse(),
                    () -> assertThat(partySinkHandler.isExist(성우.block())).isFalse(),
                    () -> assertThat(partyResult.getParticipantIds()).isNotIn(현준.block()),
                    () -> assertThat(partyResult.getParticipantIds()).contains(성우.block())
            );

        }
    }

    @Test
    @DisplayName("join 시에 없는 존재하지 않은 entryCode를 참고하면 예외를 발생한다")
    void throwPartyNotFoundException() {
        StepVerifier.create(partyService.joinAndConnectSink(현준, "123456"))
                .verifyError(PartyNotFoundException.class);
        assertThat(partySinkHandler.isExist(현준.block())).isFalse();
    }
}