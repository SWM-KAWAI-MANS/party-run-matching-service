package online.partyrun.application.domain.match.dto;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.domain.MatchMember;
import online.partyrun.application.domain.match.domain.MatchStatus;

import java.util.List;

public record MatchEvent(List<MatchMember> members, MatchStatus status) {
    public MatchEvent(Match match) {
        this(match.getMembers(), match.getStatus());
    }
}
