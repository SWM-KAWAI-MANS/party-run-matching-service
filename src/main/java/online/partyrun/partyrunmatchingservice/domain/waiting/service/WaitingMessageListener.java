package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingMessageListener implements MessageListener {
    WaitingEventService eventService;
    RedisSerializer<String> serializer = new Jackson2JsonRedisSerializer<>(String.class);

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        eventService.sendMatchEventIfExist(serializer.deserialize(message.getBody()));
    }
}
