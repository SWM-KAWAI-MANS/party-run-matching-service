package online.partyrun.partyrunmatchingservice.domain.match.domain;

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

        // match.getMembers().stream().map(MemberStatus::new).contains(MemberStatus.REDDY);
    }
}
