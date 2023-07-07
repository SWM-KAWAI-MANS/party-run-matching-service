package online.partyrun.application.domain.match.service;


import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.dto.MatchEventResponse;
import online.partyrun.application.domain.match.dto.MatchRequest;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class MatchingService {

    public void sendMatchStatus(final Mono<String> runner, final MatchRequest request) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    public Flux<MatchEventResponse> subscribe(final Mono<String> runner) {
        throw new UnsupportedOperationException("Not supported yet");

    }

    public Mono<Match> create(final List<String> runnerIds, final RunningDistance distance) {
        throw new UnsupportedOperationException("Not supported yet");
    }

}
