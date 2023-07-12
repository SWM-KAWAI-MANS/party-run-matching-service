package online.partyrun.partyrunmatchingservice.domain.match.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Match {
    @Id String id;
    List<MatchMember> members;
    int distance;
    MatchStatus status = MatchStatus.WAIT;

    @CreatedDate private LocalDateTime startAt;

    public Match(final List<MatchMember> members, int distance) {
        this.members = members;
        this.distance = distance;
    }

    public void updateMemberStatus(final String memberId, final boolean isJoin) {
        final int memberIndex = getMemberIndex(memberId);
        members.get(memberIndex).reddy();
        if (!isJoin) {
            members.get(memberIndex).cancel();
        }
        updateMatchStatus();
    }

    private void updateMatchStatus() {
        final List<MemberStatus> memberStatuses =
                members.stream().map(MatchMember::getStatus).toList();
        if (memberStatuses.contains(MemberStatus.CANCELED)) {
            status = MatchStatus.CANCEL;
        }
        if (memberStatuses.stream().allMatch(MemberStatus.READY::equals)) {
            status = MatchStatus.SUCCESS;
        }
    }

    private int getMemberIndex(final String memberId) {
        return members.stream().map(MatchMember::getId).toList().indexOf(memberId);
    }
}
