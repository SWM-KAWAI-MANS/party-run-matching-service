package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchingMemberTest {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("MatchingMember 생성 시 id가 null이거나 empty면 에외를 반환한다.")
    void throwInvalidMember(String memberId) {
        assertThatThrownBy(() -> new MatchingMember(memberId))
                .isInstanceOf(InvalidIdException.class);
    }

    @Test
    @DisplayName("ready 상태로 변경한다")
    void runReady() {
        final MatchingMember member = new MatchingMember("SAMPLE_ID");

        member.reddy();

        assertThat(member.getStatus()).isEqualTo(MatchingMemberStatus.READY);
    }

    @Test
    @DisplayName("Cancel 상태로 변경한다")
    void runCancel() {
        final MatchingMember member = new MatchingMember("SAMPLE_ID");
        member.cancel();
        assertThat(member.getStatus()).isEqualTo(MatchingMemberStatus.CANCELED);
    }
}
