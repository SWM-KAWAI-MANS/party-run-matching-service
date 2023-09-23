package online.partyrun.partyrunmatchingservice.domain.party.entity;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("party에서")
class PartyTest {
    final String manager = "방장";
    final String member = "참여자";

    @Test
    @DisplayName("join을 하면 참여자 명단에 추가를 한다.")
    void join() {
        Party party = new Party(manager, RunningDistance.M1000);
        party.join(member);

        assertThat(party.getParticipants()).contains(member);
    }

    @Test
    @DisplayName("시작을 하면 주어진 battle ID로 설정을 하고, COMPLETED로 상태로 변경한다.")
    void start() {
        Party party = new Party(manager, RunningDistance.M1000);
        String battleId = "battleId";
        party.start(battleId);

        assertAll(
                () -> assertThat(party.getBattleId()).isEqualTo(battleId),
                () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.COMPLETED)
        );
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class 참가자가_나가면 {
        Party party;

        @BeforeEach
        void beforeEach() {
            party = new Party(manager, RunningDistance.M1000);
            party.join(manager);
            party.join(member);
        }

        @Test
        @DisplayName("명단에서 제거한다")
        void deleteParticipants() {
            party.quit(member);

            assertAll(
                    () -> assertThat(party.getParticipants()).isNotIn(member),
                    () -> assertThat(party.getParticipants()).contains(manager),
                    () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.WAITING)
            );
        }

        @Test
        @DisplayName("만약 방장이면 CANCELLED로 상태를 변경한다")
        void changeCancelIfManager() {
            party.quit(manager);

            assertAll(
                    () -> assertThat(party.getParticipants()).isNotIn(manager),
                    () -> assertThat(party.getParticipants()).contains(member),
                    () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.CANCELLED)
            );
        }
    }
}