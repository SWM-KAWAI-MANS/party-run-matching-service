package online.partyrun.partyrunmatchingservice.domain.waiting.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.global.db.redis.RedisChannel;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingMessagePublisher {
    ReactiveStringRedisTemplate redisMemberTemplate;

    public void publish(String user) {
        redisMemberTemplate.convertAndSend(RedisChannel.WAITING.getChannel(), user).subscribe();
    }
}
