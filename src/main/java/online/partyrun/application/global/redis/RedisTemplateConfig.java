package online.partyrun.application.global.redis;

import online.partyrun.application.domain.match.domain.Match;
import online.partyrun.application.domain.match.domain.Runner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 관련 Template를 설정합니다.
 *
 * @author parkhyeonjun
 * @since 2023.06.29
 */
@Configuration
public class RedisTemplateConfig {

    /**
     * Match 도메인의 redis template 및 serializer를 설정합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Bean
    public ReactiveRedisTemplate<String, Match> reactiveMatchRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Match> serializer = new Jackson2JsonRedisSerializer<>(Match.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Match> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Match> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    /**
     * Runner 도메인의 redis template 및 serializer를 설정합니다.
     *
     * @author parkhyeonjun
     * @since 2023.06.29
     */
    @Bean
    public ReactiveRedisTemplate<String, Runner> reactiveRunnerRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Runner> serializer = new Jackson2JsonRedisSerializer<>(Runner.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Runner> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Runner> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
