package online.partyrun.application.domain.match.repository.redis;

import online.partyrun.application.config.redis.RedisTestConfig;
import online.partyrun.application.domain.match.domain.Runner;
import online.partyrun.application.domain.match.domain.RunnerStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfig.class)
@DisplayName("RedisRunnerRepository")
class RedisRunnerRepositoryTest {
    @Autowired
    ReactiveRedisTemplate<String, Runner> redisTemplate;

    @Autowired
    RedisRunnerRepository redisRunnerRepository;
    String matchId = "match1";

    Runner runner1 = new Runner("runner1", matchId, RunnerStatus.REDDY);
    Runner runner2 = new Runner("runner2", matchId, RunnerStatus.REDDY);
    Runner runner3 = new Runner("runner3", matchId, RunnerStatus.REDDY);

    @AfterEach
    public void cleanup() {
        redisTemplate.delete("runner*").subscribe();
    }

    @Test
    @DisplayName("저장을 수행한다")
    void successSave() {
        final Mono<Runner> findResult = redisRunnerRepository.save(runner1);

        StepVerifier.create(findResult)
                .assertNext(result -> isSameRunner(result, runner1)).verifyComplete();
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class 해당_도메인이_저장소에_존재할_떄 {
        final Runner saveResult1 = redisRunnerRepository.save(runner1).block();

        @Test
        @DisplayName("member id를 통한 조회를 수행한다")
        void successFind() {
            final Mono<Runner> findResult = redisRunnerRepository.findByMemberId(runner1.getMemberId());

            StepVerifier.create(findResult)
                    .assertNext(result -> isSameRunner(result, runner1)).verifyComplete();
        }

        @Test
        @DisplayName("삭제를 수행한다")
        void successDelete() {
            redisRunnerRepository.deleteById(saveResult1.getMemberId());

            StepVerifier.create(redisTemplate.opsForList().size("runner*"))
                    .assertNext(res -> {
                        assertThat(res).isZero();
                    }).verifyComplete();
        }

        @Test
        @DisplayName("전체조회를 수행한다")
        void successFindAll() {
            redisRunnerRepository.save(runner2).subscribe();
            redisRunnerRepository.save(runner3).subscribe();

            final Flux<Runner> findResult = redisRunnerRepository.findAllByMatchId(matchId);

            StepVerifier.create(findResult)
                    .expectNextCount(3)
                    .verifyComplete();
        }
    }

    private void isSameRunner(final Runner result, final Runner expected) {
        assertThat(result.getMemberId()).isEqualTo(expected.getMemberId());
        assertThat(result.getMatchId()).isEqualTo(expected.getMatchId());
        assertThat(result.getStatus()).isEqualTo(expected.getStatus());
    }

}