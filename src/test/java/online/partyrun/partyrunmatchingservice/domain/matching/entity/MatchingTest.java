package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.NotExistMembersException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.InvalidDistanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Matching")
class MatchingTest {
    LocalDateTime now = LocalDateTime.now();

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("matching 생성 시 members가 null이거나 empty 에외를 반환한다.")
    void throwInvalidMember(List<MatchingMember> members) {
        assertThatThrownBy(() -> new Matching(members, 1000, LocalDateTime.now()))
                .isInstanceOf(NotExistMembersException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("matching 생성 시 거리가 0 이하면 예외를 반환한다")
    void throwInvalidMember(int distance) {
        assertThatThrownBy(() -> new Matching(List.of(new MatchingMember("현준")), distance, now))
                .isInstanceOf(InvalidDistanceException.class);
    }
}
