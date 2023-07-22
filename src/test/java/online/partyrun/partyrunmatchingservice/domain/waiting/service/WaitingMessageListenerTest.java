package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.SneakyThrows;

import online.partyrun.partyrunmatchingservice.config.redis.RedisTestConfig;
import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.message.WaitingMessagePublisher;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.WaitingQueue;
import online.partyrun.partyrunmatchingservice.domain.waiting.root.WaitingUser;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@DisplayName("WaitingMessageListener")
@SpringBootTest
@Import(RedisTestConfig.class)
class WaitingMessageListenerTest {
    @Autowired WaitingMessageListener waitingMessageListener;
    @Autowired WaitingMessagePublisher waitingMessagePublisher;
    @Autowired WaitingQueue waitingQueue;

    @Autowired ServerSentEventHandler<String, WaitingStatus> sseEventHandler;

    final WaitingUser 현준 = new WaitingUser("현준", 1000);
    final WaitingUser 성우 = new WaitingUser("성우", 1000);

    @SneakyThrows
    @Test
    @DisplayName("message가 publish되면, subscribe를 진행한다")
    void runOnMessage() {
        waitingMessagePublisher.publish(현준);
        waitingMessagePublisher.publish(성우);

        // assertThat(sseEventHandler.getConnectors()).contains(현준.userId(), 성우.userId());
    }
}
