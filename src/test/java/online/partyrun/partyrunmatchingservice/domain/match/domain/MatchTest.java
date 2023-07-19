package online.partyrun.partyrunmatchingservice.domain.match.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import online.partyrun.partyrunmatchingservice.domain.match.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.match.exception.InvalidMembersException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

@DisplayName("Match")
class MatchTest {

    final List<MatchMember> members =
            Stream.of("member1", "member2", "member3").map(MatchMember::new).toList();

    @Test
    @DisplayName("member status 업데이트를 수행하는가")
    void runUpdate() {
        Match match = new Match(members, 1000);

        match.updateMemberStatus("member1", true);

        final boolean hasReady =
                match.getMembers().stream()
                        .map(MatchMember::getStatus)
                        .anyMatch(status -> status.equals(MemberStatus.READY));
        assertThat(hasReady).isTrue();
    }

    @Test
    @DisplayName("distance 값이 올바르지 않으면 예외를 반환하는가")
    void runValidateDistance() {
        assertThatThrownBy(() -> new Match(members, 0))
                .isInstanceOf(InvalidDistanceException.class);
    }

    @Test
    @DisplayName("members 값이 올바르지 않으면 예외를 반환하는가")
    void runValidateMembers() {
        assertAll(
                () ->
                        assertThatThrownBy(() -> new Match(null, 1000))
                                .isInstanceOf(InvalidMembersException.class),
                () ->
                        assertThatThrownBy(() -> new Match(List.of(), 1000))
                                .isInstanceOf(InvalidMembersException.class));
    }
}
