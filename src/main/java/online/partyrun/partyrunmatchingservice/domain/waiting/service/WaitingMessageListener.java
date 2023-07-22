package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.WaitingQueue;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.RunningDistance;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingUser;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingMessageListener implements MessageListener {

    WaitingEventService eventService;
    WaitingQueue queue;
    RedisSerializer<WaitingUser> serializer;
    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        queue.add(serializer.deserialize(message.getBody()));
        processMessages();
    }

    @Synchronized
    private void processMessages() {
        Arrays.stream(RunningDistance.values())
                .forEach(
                        distance -> {
                            if (queue.satisfyCount(distance)) {
                                List<String> members = queue.poll(distance);
                                // TODO 매칭 생성 보내기
                                eventService.sendMatchEvent(members);
                            }
                        });
    }
}
