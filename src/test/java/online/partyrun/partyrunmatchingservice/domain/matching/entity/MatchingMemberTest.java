package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidIdException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class MatchingMemberTest {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("MatchingMember 생성 시 id가 null이거나 empty면 에외를 반환한다.")
    void throwInvalidMember(String memberId) {
        assertThatThrownBy(() -> new MatchingMember(memberId))
                .isInstanceOf(InvalidIdException.class);
    }
}
