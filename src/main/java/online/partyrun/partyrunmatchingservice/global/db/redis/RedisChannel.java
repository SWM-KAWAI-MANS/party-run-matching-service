package online.partyrun.partyrunmatchingservice.global.db.redis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.redis.listener.ChannelTopic;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum RedisChannel {
    WAITING(new ChannelTopic("waiting")),
    WAITING_COUNT(new ChannelTopic("waiting-count")),
    DISTRIBUTED_LOCK(new ChannelTopic("distributed-lock"));
    ChannelTopic topic;

    public String getChannel() {
        return topic.getTopic();
    }
}
