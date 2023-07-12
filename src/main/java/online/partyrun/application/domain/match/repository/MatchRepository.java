package online.partyrun.application.domain.match.repository;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.domain.MatchStatus;
import online.partyrun.application.domain.match.domain.MemberStatus;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MatchRepository extends ReactiveMongoRepository<Match, String> {
    Mono<Match> findByMembersIdAndMembersStatus(String id, MemberStatus status);
    Flux<Match> findAllByStatus(MatchStatus status);
}
