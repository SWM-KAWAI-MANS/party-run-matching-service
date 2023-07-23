package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidMembersException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Matching")
class MatchingTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("matching 생성 시 members가 null이거나 empty 에외를 반환한다.")
    void throwInvalidMember(List<MatchingMember> members) {
        assertThatThrownBy(() -> new Matching(members, 1000))
                .isInstanceOf(InvalidMembersException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("matching 생성 시 거리가 0 이하면 예외를 반환한다")
    void throwInvalidMember(int distance) {
        assertThatThrownBy(() -> new Matching(List.of(new MatchingMember("현준")), distance))
                .isInstanceOf(InvalidDistanceException.class);
    }
}