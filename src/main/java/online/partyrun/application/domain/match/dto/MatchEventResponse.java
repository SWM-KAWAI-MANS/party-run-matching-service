package online.partyrun.application.domain.match.dto;

import online.partyrun.application.domain.match.domain.MatchEvent;

public record MatchEventResponse(String status, String message) {
    public MatchEventResponse(MatchEvent matchEvent) {
        this(matchEvent.name(), matchEvent.getMessage());
    }
}
