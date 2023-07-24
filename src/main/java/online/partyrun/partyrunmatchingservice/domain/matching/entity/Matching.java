package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidMembersException;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Matching {
    private static final int MIN_DISTANCE = 1;
    @Id String id;
    List<MatchingMember> members;
    int distance;
    MatchingStatus status = MatchingStatus.WAIT;

    public Matching(final List<MatchingMember> members, int distance) {
        validateMembers(members);
        validateDistance(distance);
        this.members = members;
        this.distance = distance;
    }

    private void validateDistance(int distance) {
        if (distance < MIN_DISTANCE) {
            throw new InvalidDistanceException();
        }
    }

    private void validateMembers(List<MatchingMember> members) {
        if (Objects.isNull(members) || members.isEmpty()) {
            throw new InvalidMembersException();
        }
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
        final List<MatchingMemberStatus> memberStatuses =
                members.stream().map(MatchingMember::getStatus).toList();
        if (memberStatuses.contains(MatchingMemberStatus.CANCELED)) {
            status = MatchingStatus.CANCEL;
        }
        if (memberStatuses.stream().allMatch(MatchingMemberStatus.READY::equals)) {
            status = MatchingStatus.SUCCESS;
        }
    }

    private int getMemberIndex(final String memberId) {
        return members.stream().map(MatchingMember::getId).toList().indexOf(memberId);
    }
}
