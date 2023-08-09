package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMember;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingMemberStatus;

public record MatchingMemberResponse(String id, MatchingMemberStatus status) {

    public MatchingMemberResponse(MatchingMember member) {
        this(member.getId(), member.getStatus());
    }
}
