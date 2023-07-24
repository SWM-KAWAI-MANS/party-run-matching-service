package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidMembersException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

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

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class 참여자가_주어졌을_떄 {

        List<String> members = List.of("현준", "성우", "준혁");
        final Matching matching =
                new Matching(members.stream().map(MatchingMember::new).toList(), 1000);

        @Test
        @DisplayName("모든 맴버들이 ready 상태 시에 success 상태로 변경한다")
        void runChangeMatchingSuccess() {
            members.forEach(member -> matching.updateMemberStatus(member, MatchingMemberStatus.READY));

            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.SUCCESS);
        }

        @Test
        @DisplayName("한 명이라도 cancel 시에 cancel 상태로 변경한다")
        void runChangeMatchingCancel() {
            matching.updateMemberStatus(members.get(1), MatchingMemberStatus.CANCELED);
            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.CANCEL);
        }
    }
}
