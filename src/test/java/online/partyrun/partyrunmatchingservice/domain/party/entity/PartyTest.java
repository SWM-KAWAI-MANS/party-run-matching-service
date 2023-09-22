package online.partyrun.partyrunmatchingservice.domain.party.entity;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("party에서")
class PartyTest {
    @Test
    @DisplayName("join을 하면 참여자 명단에 추가를 한다.")
    void join() {
        Party party = new Party("leader", RunningDistance.M1000);
        String newMember = "newMember";
        party.join(newMember);

        assertThat(party.getParticipants()).contains(newMember);
    }

    @Test
    @DisplayName("시작을 하면 주어진 battle ID로 설정을 하고, COMPLETED로 상태로 변경한다.")
    void start() {
        Party party = new Party("leader", RunningDistance.M1000);
        String battleId = "battleId";
        party.start(battleId);

        assertAll(
                () -> assertThat(party.getBattleId()).isEqualTo(battleId),
                () -> assertThat(party.getStatus()).isEqualTo(PartyStatus.COMPLETED)
        );
    }
}