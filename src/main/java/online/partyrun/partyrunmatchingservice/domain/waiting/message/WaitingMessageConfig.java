package online.partyrun.partyrunmatchingservice.domain.waiting.message;

import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingMember;
import online.partyrun.partyrunmatchingservice.domain.waiting.service.WaitingMessageListener;
import online.partyrun.partyrunmatchingservice.global.db.redis.RedisChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class WaitingMessageConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory, WaitingMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, RedisChannel.WAITING.getTopic());
        return container;
    }

    @Bean
    public RedisTemplate<String, WaitingMember> redisMemberTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, WaitingMember> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(WaitingMember.class));
        return template;
    }
}
