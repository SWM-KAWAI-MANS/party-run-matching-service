package online.partyrun.partyrunmatchingservice.domain.match.dto;

import online.partyrun.partyrunmatchingservice.domain.match.domain.Match;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchMember;
import online.partyrun.partyrunmatchingservice.domain.match.domain.MatchStatus;

import java.util.List;

public record MatchEvent(List<MatchMember> members, MatchStatus status) {
    public MatchEvent(Match match) {
        this(match.getMembers(), match.getStatus());
    }
}
