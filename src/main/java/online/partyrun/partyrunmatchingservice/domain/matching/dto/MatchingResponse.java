package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;

import java.util.List;

public record MatchingResponse(String id, List<MatchingMemberResponse> members, int distance) {
    public MatchingResponse(Matching matching) {
        this(
                matching.getId(),
                matching.getMembers().stream().map(MatchingMemberResponse::new).toList(),
                matching.getDistance());
    }
}
