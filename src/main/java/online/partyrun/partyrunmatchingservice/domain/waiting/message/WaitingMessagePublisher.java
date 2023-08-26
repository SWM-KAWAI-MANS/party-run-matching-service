package online.partyrun.partyrunmatchingservice.domain.waiting.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import online.partyrun.partyrunmatchingservice.global.db.redis.RedisChannel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingMessagePublisher {
    RedisTemplate<String, WaitingMember> redisTemplate;

    public void publish(String user) {
        redisTemplate.convertAndSend(RedisChannel.WAITING.getChannel(), user);
    }
}
