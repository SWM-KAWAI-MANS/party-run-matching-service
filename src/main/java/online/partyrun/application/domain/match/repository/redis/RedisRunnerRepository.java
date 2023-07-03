package online.partyrun.application.domain.match.repository.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.application.domain.match.domain.Runner;
import online.partyrun.application.domain.match.repository.RunnerRepository;
import online.partyrun.application.global.redis.RedisOperationException;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RunnerRepository} 를 구현한 클래스입니다.{@link Runner}를 Redis 환경에서 저장하는 기능을 제공합니다.
 *
 * @author parkhyeonjun
 * @see online.partyrun.application.global.redis.RedisTemplateConfig
 * @see RedisOperationException
 * @since 2023.06.29
 */
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RedisRunnerRepository implements RunnerRepository {
    ReactiveRedisTemplate<String, Runner> redisTemplate;

    /**
     * matchId에 해당하는 runner 목록을 전체 조회합니다 {@link Flux}로 wrapping한 {@link Runner}를 반환합니다.
     *
     * @param matchId 검색할 대상 matchId
     * @return matchId에 해당하는 runner 목록, {@link Flux}로 rapping한 {@link Runner}
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Flux<Runner> findAllByMatchId(final String matchId) {
        return redisTemplate
                .keys("runner*")
                .flatMap(this::findByMemberId)
                .filter(r -> r.getMatchId().equals(matchId));
    }

    /**
     * id에 해당하는 runner를 삭제합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public void deleteById(final String memberId) {
        redisTemplate.delete(memberId).subscribe();
    }

    /**
     * runner 저장을 수행합니다. 성공시 {@link Mono}로 wrapping한 {@link Runner}를 반환합니다. 에러 발생시 {@link
     * RedisOperationException}을 반환합니다.
     *
     * @param runner 저장할 도메인
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Mono<Runner> save(final Runner runner) {

        return redisTemplate
                .opsForValue()
                .set(runner.getMemberId(), runner)
                .handle(
                        (success, sink) -> {
                            if (Boolean.TRUE.equals(success)) {
                                sink.next(runner);
                                return;
                            }
                            sink.error(new RedisOperationException());
                        });
    }

    /**
     * memberId에 해당하는 runner를 조회합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Override
    public Mono<Runner> findByMemberId(final String runnerId) {
        return redisTemplate.opsForValue().get(runnerId);
    }
}
