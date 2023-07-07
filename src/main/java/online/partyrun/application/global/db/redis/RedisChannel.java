package online.partyrun.application.global.db.redis;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.listener.ChannelTopic;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RedisChannel {
    WAITING(new ChannelTopic("waiting"), "waiting"),
    WAITING_COUNT(new ChannelTopic("waiting-count"), "waiting-count");

    ChannelTopic topic;
    String channel;
}

