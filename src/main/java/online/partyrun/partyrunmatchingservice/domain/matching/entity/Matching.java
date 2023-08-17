package online.partyrun.partyrunmatchingservice.domain.matching.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.battle.service.external.exception.BattleAlreadyExistException;
import online.partyrun.partyrunmatchingservice.domain.matching.exception.NotExistMembersException;
import online.partyrun.partyrunmatchingservice.domain.waiting.exception.InvalidDistanceException;
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
    @Id
    String id;
    List<MatchingMember> members;
    int distance;
    LocalDateTime startAt;
    String battleId;

    public Matching(final List<MatchingMember> members, int distance, LocalDateTime startAt) {
        validateMembers(members);
        validateDistance(distance);
        this.members = members;
        this.distance = distance;
        this.startAt = startAt;
    }

    private void validateDistance(int distance) {
        if (distance < MIN_DISTANCE) {
            throw new InvalidDistanceException(distance);
        }
    }

    private void validateMembers(List<MatchingMember> members) {
        if (Objects.isNull(members) || members.isEmpty()) {
            throw new NotExistMembersException();
        }
    }

    public void cancel() {
        members.forEach(member -> member.changeStatus(MatchingMemberStatus.CANCELED));
    }

    public MatchingStatus getStatus() {
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

    public void setBattleId(final String battleId) {
        if (Objects.nonNull(this.battleId)) {
            throw new BattleAlreadyExistException();
        }
        this.battleId = battleId;
    }
}
