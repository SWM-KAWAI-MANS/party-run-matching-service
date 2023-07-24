package online.partyrun.partyrunmatchingservice.domain.waiting.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import online.partyrun.partyrunmatchingservice.domain.waiting.dto.WaitingStatus;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WaitingEventService {
    WaitingSinkHandler waitingSinkHandler;

    public void register(final String id) {
        waitingSinkHandler.create(id);
    }

    public Flux<WaitingStatus> getEventStream(Mono<String> member) {
        return member.flatMapMany(
                id ->
                        waitingSinkHandler
                                .connect(id)
                                .doOnNext(
                                        status -> {
                                            if (status.isCompleted()) {
                                                waitingSinkHandler.complete(id);
                                            }
                                        })
                                .doOnSubscribe(
                                        s ->
                                                waitingSinkHandler.sendEvent(
                                                        id, WaitingStatus.CONNECTED)));
    }

    public void sendMatchEvent(List<String> members) {
        members.forEach(member -> waitingSinkHandler.sendEvent(member, WaitingStatus.MATCHED));
    }
}
