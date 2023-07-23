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
        if (distance <= 0) {
            throw new InvalidDistanceException();
        }
    }

    private void validateMembers(List<MatchingMember> members) {
        if (Objects.isNull(members) || members.isEmpty()) {
            throw new InvalidMembersException();
        }
    }
}
