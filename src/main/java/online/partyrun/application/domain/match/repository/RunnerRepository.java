package online.partyrun.application.domain.match.repository;

import online.partyrun.application.domain.match.domain.Runner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Runner에 해당하는 저장소 interface 입니다.
 *
 * @author parkhyeonjun
 * @see online.partyrun.application.domain.match.repository.redis.RedisRunnerRepository
 * @see Runner
 * @since 2023.06.29
 */
public interface RunnerRepository {
    Flux<Runner> findAllByMatchId(String matchId);

    void deleteById(String matchId);

    Mono<Runner> save(Runner runner);

    Mono<Runner> findByMemberId(String runnerId);
}
