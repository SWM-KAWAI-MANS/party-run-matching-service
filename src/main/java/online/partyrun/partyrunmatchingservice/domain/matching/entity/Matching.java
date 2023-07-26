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

    public void cancel() {
        status = MatchingStatus.CANCEL;
        members.forEach(member -> member.changeStatus(MatchingMemberStatus.CANCELED));
    }

    public void updateStatus() {
        this.status = generateMatchingStatus();
    }

    public boolean isWait() {
        return this.status.isWait();
    }

    private MatchingStatus generateMatchingStatus() {
        final List<MatchingMemberStatus> memberStatuses =
                members.stream().map(MatchingMember::getStatus).toList();
        if (memberStatuses.contains(MatchingMemberStatus.CANCELED)) {
            return MatchingStatus.CANCEL;
        }
        if (memberStatuses.stream().allMatch(MatchingMemberStatus.READY::equals)) {
            return MatchingStatus.SUCCESS;
        }
        return MatchingStatus.WAIT;
    }
}
