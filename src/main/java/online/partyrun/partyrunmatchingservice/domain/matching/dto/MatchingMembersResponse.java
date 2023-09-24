package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;

import java.util.List;

public record MatchingMembersResponse(List<MatchingMemberResponse> members) {
    public MatchingMembersResponse(Matching matching) {
        this(matching.getMembers().stream().map(MatchingMemberResponse::new).toList());
    }
}
