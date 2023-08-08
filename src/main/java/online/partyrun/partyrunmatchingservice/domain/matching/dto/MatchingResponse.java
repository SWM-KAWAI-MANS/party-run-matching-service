package online.partyrun.partyrunmatchingservice.domain.matching.dto;

import online.partyrun.partyrunmatchingservice.domain.matching.entity.Matching;
import online.partyrun.partyrunmatchingservice.domain.matching.entity.MatchingStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MatchingResponse(String id,
                               List<MatchingMemberResponse> members,
                               int distance,
                               MatchingStatus status,
                               LocalDateTime startAt) {
    public MatchingResponse(Matching matching) {
        this(matching.getId(),
                matching.getMembers().stream().map(MatchingMemberResponse::new).toList(),
                matching.getDistance(),
                matching.getStatus(),
                matching.getStartAt());
    }
}
