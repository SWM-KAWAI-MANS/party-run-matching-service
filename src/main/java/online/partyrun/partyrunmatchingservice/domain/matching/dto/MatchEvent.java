package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;

import java.util.List;
import java.util.Objects;

public record MatchEvent(List<MatchingMember> members, MatchingStatus status, String battleId) {
    public MatchEvent(Matching matching) {
        this(matching.getMembers(), matching.getStatus(), matching.getBattleId());
    }

    public boolean isComplete() {
        return status.equals(MatchingStatus.CANCEL) || Objects.nonNull(battleId);
    }
}
