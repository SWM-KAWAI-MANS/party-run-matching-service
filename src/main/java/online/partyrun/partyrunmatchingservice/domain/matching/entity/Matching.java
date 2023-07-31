package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidDistanceException;
import online.partyrun.partyrunmatchingservice.domain.matching.exception.InvalidMembersException;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Matching {
    private static final int MIN_DISTANCE = 1;
    private static final int TIMEOUT_HOUR = 2;
    @Id String id;
    List<MatchingMember> members;
    int distance;
    MatchingStatus status = MatchingStatus.WAIT;
    LocalDateTime startAt;

    public Matching(final List<MatchingMember> members, int distance, LocalDateTime startAt) {
        validateMembers(members);
        validateDistance(distance);
        this.members = members;
        this.distance = distance;
        this.startAt = startAt;
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
        if (this.members.stream().anyMatch(MatchingMember::isCanceled)) {
            return MatchingStatus.CANCEL;
        }
        if (this.members.stream().allMatch(MatchingMember::isReady)) {
            return MatchingStatus.SUCCESS;
        }
        return MatchingStatus.WAIT;
    }

    public boolean isTimeOut(LocalDateTime now) {
        return this.startAt.isBefore(now.minusHours(TIMEOUT_HOUR));
    }
}
