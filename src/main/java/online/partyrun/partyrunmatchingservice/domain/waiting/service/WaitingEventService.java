package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;
import online.partyrun.partyrunmatchingservice.global.sse.ServerSentEventHandler;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingEventService {
    ServerSentEventHandler<String, WaitingStatus> sseHandler;

    public void register(final String id) {
        sseHandler.create(id);
    }

    public Flux<WaitingStatus> getEventStream(Mono<String> member) {
        return member.flatMapMany(
                id ->
                        sseHandler
                                .connect(id)
                                .doOnSubscribe(
                                        s -> sseHandler.sendEvent(id, WaitingStatus.CONNECTED)));
    }

    public void sendMatchEvent(List<String> members) {
        members.forEach(member -> sseHandler.sendEvent(member, WaitingStatus.MATCHED));
    }
}
