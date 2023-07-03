package online.partyrun.application.domain.match.repository.redis;

import static org.assertj.core.api.Assertions.assertThat;

import online.partyrun.application.config.redis.RedisTestConfig;
import online.partyrun.application.domain.match.domain.Match;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(RedisTestConfig.class)
@DisplayName("RedisMatchRepository")
class RedisMatchRepositoryTest {
    @Autowired ReactiveRedisTemplate<String, Match> redisTemplate;

    @Autowired RedisMatchRepository redisMatchRepository;

    Match match = new Match(1000);

    @Test
    @DisplayName("저장을 수행한다")
    void successSave() {
        final Mono<Match> result = redisMatchRepository.save(match);

        StepVerifier.create(result)
                .assertNext(
                        res -> {
                            assertThat(res.getDistance()).isEqualTo(match.getDistance());
                            assertThat(res.getId()).isNotBlank();
                        })
                .verifyComplete();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 해당_도메인이_저장소에_존재할_떄 {
        final Match saveResult = redisMatchRepository.save(match).block();

        @Test
        @DisplayName("조회를 수행한다")
        void successFind() {
            final Mono<Match> result = redisMatchRepository.findById(saveResult.getId());

            StepVerifier.create(result)
                    .assertNext(
                            res -> {
                                assertThat(res.getId()).isEqualTo(saveResult.getId());
                                assertThat(res.getDistance()).isEqualTo(saveResult.getDistance());
                            })
                    .verifyComplete();
        }

        @Test
        @DisplayName("삭제를 수행한다")
        void successDelete() {
            redisMatchRepository.deleteById(saveResult.getId());

            StepVerifier.create(redisTemplate.opsForList().size("*"))
                    .assertNext(
                            res -> {
                                assertThat(res).isZero();
                            })
                    .verifyComplete();
        }
    }
}
