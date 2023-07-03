package online.partyrun.application.domain.match.repository.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.repository.MatchRepository;
import online.partyrun.application.global.redis.RedisOperationException;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

/**
 * {@link MatchRepository} 를 구현한 클래스입니다.{@link Match}를 Redis 환경에서 저장하는 기능을 제공합니다.
 *
 * @author parkhyeonjun
 * @see online.partyrun.application.global.redis.RedisTemplateConfig
 * @see RedisOperationException
 * @since 2023.06.29
 */
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisMatchRepository implements MatchRepository {
    ReactiveRedisTemplate<String, Match> redisTemplate;

    /**
     * match 저장을 수행합니다. 성공시 {@link Mono}로 wrapping한 {@link Match}를 반환합니다. 에러 발생시 {@link
     * RedisOperationException}을 반환합니다.
     *
     * @param match 저장할 도메인
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Mono<Match> save(final Match match) {
        return redisTemplate
                .opsForValue()
                .set(match.getId(), match)
                .handle(
                        (success, sink) -> {
                            if (success) {
                                sink.next(match);
                                return;
                            }
                            sink.error(new RedisOperationException());
                        });
    }

    /**
     * matchId에 해당하는 도메인을 삭제합니다.
     *
     * @param matchId 삭제하는 도메인의 id
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void deleteById(final String matchId) {
        redisTemplate.delete(matchId).subscribe();
    }

    /**
     * matchId에 해당하는 도메인을 조회합니다. 조회 성공시 {@link Mono} {@link Match}를 반환합니다.
     *
     * @param matchId 삭제하는 도메인의 id
     * @return id에 해당되는 {@link Mono} {@link Match}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Mono<Match> findById(final String matchId) {
        return redisTemplate.opsForValue().get(matchId);
    }
}
