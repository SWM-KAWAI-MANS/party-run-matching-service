package online.partyrun.partyrunmatchingservice.domain.party.entity;

import online.partyrun.partyrunmatchingservice.domain.party.exception.IllegalArgumentIdException;
import online.partyrun.partyrunmatchingservice.domain.party.exception.NotSatisfyMemberCountException;
import online.partyrun.partyrunmatchingservice.domain.party.exception.PartyClosedException;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        assertThat(party.getParticipantIds()).contains(member);
    }

    @Test
    @DisplayName("시작을 하면 주어진 battle ID로 설정을 하고, COMPLETED로 상태로 변경한다.")
    void start() {
        Party party = new Party(manager, RunningDistance.M1000);
        party.join(manager);
        party.join(member);
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
                    () -> assertThat(party.getParticipantIds()).isNotIn(member),
                    () -> assertThat(party.getParticipantIds()).contains(manager),
                    () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.WAITING)
            );
        }

        @Test
        @DisplayName("만약 방장이면 CANCELLED로 상태를 변경한다")
        void changeCancelIfManager() {
            party.quit(manager);

            assertAll(
                    () -> assertThat(party.getParticipantIds()).isNotIn(manager),
                    () -> assertThat(party.getParticipantIds()).contains(member),
                    () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.CANCELLED)
            );
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class Argument값이_잘못됐으면 {
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("예외를 발생한다")
        void throwException(String id) {
            Party party = new Party(manager, RunningDistance.M1000);
            assertAll(
                    () -> assertThatThrownBy(() -> party.quit(id)).isInstanceOf(IllegalArgumentIdException.class),
                    () -> assertThatThrownBy(() -> party.join(id)).isInstanceOf(IllegalArgumentIdException.class),
                    () -> assertThatThrownBy(() -> party.start(id)).isInstanceOf(IllegalArgumentIdException.class)
            );
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class Party가_시작할_수_있는_상태가_아닐_때 {
        final String battleId = "battleId";
        @Test
        @DisplayName("party가 이미 completed했을 때 start 시에 예외를 발생한다")
        void throwExceptionIfPartyComplete() {
            Party party = new Party(manager, RunningDistance.M1000);
            party.join(manager);
            party.join(member);
            party.start(battleId);

            assertThatThrownBy(() -> party.start(battleId))
                    .isInstanceOf(PartyClosedException.class);
        }

        @Test
        @DisplayName("party가 이미 canceled했을 때 start 시에 예외를 발생한다")
        void throwExceptionIfPartyCanceled() {
            Party party = new Party(manager, RunningDistance.M1000);
            party.join(manager);
            party.quit(manager);
            assertThatThrownBy(() -> party.start(battleId))
                    .isInstanceOf(PartyClosedException.class);
        }

        @Test
        @DisplayName("party가 시작할 수 있는 인원에 총족을 못하면 예외를 발생한다")
        void throwExceptionIfPartyNotSatisfyMemberCount() {
            Party party = new Party(manager, RunningDistance.M1000);
            party.join(manager);

            assertThatThrownBy(() -> party.start(battleId))
                    .isInstanceOf(NotSatisfyMemberCountException.class);
        }
    }
}