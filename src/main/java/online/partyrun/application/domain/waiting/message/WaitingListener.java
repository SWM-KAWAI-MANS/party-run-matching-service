package online.partyrun.application.domain.waiting.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import online.partyrun.application.domain.match.service.MatchService;
import online.partyrun.application.domain.waiting.domain.RunningDistance;
import online.partyrun.application.domain.waiting.domain.WaitingEvent;
import online.partyrun.application.domain.waiting.domain.WaitingUser;
import online.partyrun.application.domain.waiting.repository.SubscribeBuffer;
import online.partyrun.application.global.handler.ServerSentEventHandler;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class WaitingListener implements MessageListener {
    ServerSentEventHandler<String, WaitingEvent> waitingEventHandler;
    SubscribeBuffer buffer;
    MatchService matchService;
    RedisSerializer<WaitingUser> serializer;
    static int SATISFY_COUNT = 2;

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        buffer.add(serializer.deserialize(message.getBody()));
        processMessages();
    }

    private void processMessages() {
        log.info("processMessages started");
        Arrays.stream(RunningDistance.values())
                .forEach(
                        distance -> {
                            if (buffer.satisfyCount(distance, SATISFY_COUNT)) {
                                List<String> memberIds = buffer.flush(distance, SATISFY_COUNT);
                                // 매칭 생성 보내기
                                matchService.create(memberIds, distance).subscribe();
                                // Event 추가하기
                                memberIds.forEach(
                                        m ->
                                                waitingEventHandler.sendEvent(
                                                        m, WaitingEvent.MATCHED));
                            }
                        });
    }
}
