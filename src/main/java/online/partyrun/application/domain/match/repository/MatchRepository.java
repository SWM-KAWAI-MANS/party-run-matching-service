package online.partyrun.application.domain.match.repository;

import online.partyrun.application.domain.match.domain.Match;

import reactor.core.publisher.Mono;

/**
 * Match에 관련한 저장소 interface 입니다.
 *
 * @author parkhyeonjun
 * @see online.partyrun.application.domain.match.repository.redis.RedisMatchRepository
 * @see Match
 * @since 2023.06.29
 */
public interface MatchRepository {
    Mono<Match> save(Match match);

    void deleteById(String matchId);

    Mono<Match> findById(String matchId);
}
