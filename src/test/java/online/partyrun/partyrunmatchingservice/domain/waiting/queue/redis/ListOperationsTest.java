package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ListOperations;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfig.class)
class ListOperationsTest {
    @Autowired
    ListOperations<String, Waiting> listOperations;

    @Test
    @DisplayName("lpush를 수행한다")
    void runLPush() {
        String distance1 = "1000";
        String distance2 = "2000";
        String distance3 = "3000";
        String distance4 = "4000";

        listOperations.leftPush(distance1, new Waiting("qwer", 1000));
        listOperations.leftPush(distance2, new Waiting("asdf", 1000));
        listOperations.leftPush(distance3, new Waiting("zxcv", 1000));
        listOperations.leftPush(distance3, new Waiting("kkkk", 1000));
        listOperations.leftPush(distance4, new Waiting("bnbn", 1000));

        final Waiting waiting = listOperations.rightPop(distance1);
        assertThat(waiting.getId()).isEqualTo("qwer");
        final Waiting waiting1 = listOperations.rightPop(distance3);
        assertThat(waiting1.getId()).isEqualTo("zxcv");
        final Waiting waiting2 = listOperations.rightPop(distance3);
        assertThat(waiting2.getId()).isEqualTo("kkkk");
    }
}