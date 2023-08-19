package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Getter
@RedisHash("waiting")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Waiting {
    @Id
    private String id;
    private int distance;
}
