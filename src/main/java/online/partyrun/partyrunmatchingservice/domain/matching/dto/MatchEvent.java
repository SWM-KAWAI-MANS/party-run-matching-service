package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;

import java.util.List;

public record MatchEvent(List<MatchingMember> members, MatchingStatus status, String battleId) {
    public MatchEvent(Matching matching) {
        this(matching.getMembers(), matching.getStatus(), matching.getBattleId());
    }
}
