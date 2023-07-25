package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidIdException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class MatchingMemberTest {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("MatchingMember 생성 시 id가 null이거나 empty면 에외를 반환한다.")
    void throwInvalidMember(String memberId) {
        assertThatThrownBy(() -> new MatchingMember(memberId))
                .isInstanceOf(InvalidIdException.class);
    }

    @ParameterizedTest
    @DisplayName("상태를 변경한다")
    @EnumSource
    void runChangeStatus(MatchingMemberStatus status) {
        final MatchingMember member = new MatchingMember("SAMPLE_ID");

        member.changeStatus(status);

        assertThat(member.getStatus()).isEqualTo(status);
    }
}
