package online.partyrun.partyrunmatchingservice.domain.waiting.queue.redis;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisWaitingConfig {
    @Bean
    public ReactiveListOperations<RunningDistance, String> waitingReactiveListOperations(
            ReactiveRedisConnectionFactory factory) {
        final RedisSerializer<RunningDistance> keySerializer = new Jackson2JsonRedisSerializer<>(RunningDistance.class);
        final RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        RedisSerializationContext<RunningDistance, String> serializationContext = RedisSerializationContext
                .<RunningDistance, String>newSerializationContext()
                .key(keySerializer)
                .value(stringSerializer)
                .hashKey(keySerializer)
                .hashValue(stringSerializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, serializationContext).opsForList();
    }

}
