package online.partyrun.application.domain.waiting.message;

import online.partyrun.application.domain.waiting.domain.WaitingUser;
import online.partyrun.application.global.redis.RedisChannel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
public class WaitingMessageConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory, WaitingListener subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, RedisChannel.WAITING.getTopic());
        return container;
    }

    @Bean
    public RedisSerializer<WaitingUser> waitingUserSerializer() {
        return new Jackson2JsonRedisSerializer<>(WaitingUser.class);
    }

    @Bean
    public RedisTemplate<String, WaitingUser> redisMemberTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, WaitingUser> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        setSerializer(template);

        return template;
    }

    private void setSerializer(final RedisTemplate<String, WaitingUser> template) {
        template.afterPropertiesSet();
        setKeySerializer(template);
        setValueSerializer(template);
    }

    private void setValueSerializer(final RedisTemplate<String, WaitingUser> template) {
        template.setDefaultSerializer(waitingUserSerializer());
        template.setValueSerializer(waitingUserSerializer());
        template.setHashValueSerializer(waitingUserSerializer());
    }

    private void setKeySerializer(final RedisTemplate<String, WaitingUser> template) {
        final StringRedisSerializer keySerializer =
                new StringRedisSerializer(StandardCharsets.UTF_8);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
    }
}
