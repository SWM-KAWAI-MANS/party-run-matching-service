package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.domain.waiting.queue.WaitingQueue;
import online.partyrun.partyrunmatchingservice.global.sse.SinkHandlerTemplate;

import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingSinkHandler extends SinkHandlerTemplate<String, WaitingStatus> {
    WaitingQueue waitingQueue;

    @Override
    protected Runnable onCancel(String key) {
        return () -> waitingQueue.delete(key);
    }
}
