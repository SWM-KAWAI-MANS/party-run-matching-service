package online.partyrun.application.domain.match.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Match {
    @Id String id;
    List<MatchMember> members;
    int distance;
    MatchStatus status = MatchStatus.WAIT;

    public Match(final List<MatchMember> members, int distance) {
        this.members = members;
        this.distance = distance;
    }

    public void updateMemberStatus(final String memberId, final boolean isJoin) {
        final int memberIndex = getMemberIndex(memberId);
        if (isJoin) {
            members.get(memberIndex).reddy();
        } else {
            members.get(memberIndex).cancel();
        }
        updateMatchStatus();
    }

    private void updateMatchStatus() {
        final List<MemberStatus> memberStatuses =
                members.stream().map(MatchMember::getStatus).toList();
        if (memberStatuses.contains(MemberStatus.CANCELLED)) {
            status = MatchStatus.CANCEL;
        }
        if (memberStatuses.stream().allMatch(MemberStatus.READY::equals)) {
            status = MatchStatus.SUCCESS;
        }
    }

    private int getMemberIndex(String memberId) {
        return members.stream().map(MatchMember::getId).toList().indexOf(memberId);
    }
}
