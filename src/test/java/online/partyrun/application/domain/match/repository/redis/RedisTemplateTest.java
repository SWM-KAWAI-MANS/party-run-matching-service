package online.partyrun.application.domain.match.repository.redis;

import online.partyrun.application.config.redis.RedisTestConfig;
import online.partyrun.application.domain.match.domain.Runner;
import online.partyrun.application.domain.match.domain.RunnerStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

// 학습테스트
@SpringBootTest
@Import(RedisTestConfig.class)
class RedisTemplateTest {

    @Autowired ReactiveRedisTemplate<String, Runner> redisTemplate;

    @Test
    @DisplayName("전체 조회 수행")
    void successFindAll() {
        String matchId = "1";
        Runner runner1 = new Runner("runner1", matchId, RunnerStatus.REDDY);
        Runner runner2 = new Runner("runner2", matchId, RunnerStatus.REDDY);
        Runner runner3 = new Runner("runner3", matchId, RunnerStatus.REDDY);

        redisTemplate.opsForValue().set(runner1.getMemberId(), runner1).subscribe();
        redisTemplate.opsForValue().set(runner2.getMemberId(), runner2).subscribe();
        redisTemplate.opsForValue().set(runner3.getMemberId(), runner3).subscribe();

        final Flux<String> findResult = redisTemplate.keys("runner*");

        StepVerifier.create(findResult).expectNextCount(3).verifyComplete();
    }
}
